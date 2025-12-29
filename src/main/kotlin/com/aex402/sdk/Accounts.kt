/**
 * AeX402 SDK - Account Parsing
 *
 * Functions to deserialize account data from on-chain state.
 */
package com.aex402.sdk

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

// ============================================================================
// Buffer Reading Helpers
// ============================================================================

private fun ByteArray.readU8(offset: Int): Int = this[offset].toInt() and 0xFF

private fun ByteArray.readU16LE(offset: Int): Int {
    return (this[offset].toInt() and 0xFF) or
           ((this[offset + 1].toInt() and 0xFF) shl 8)
}

private fun ByteArray.readI16LE(offset: Int): Short {
    return ((this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8)).toShort()
}

private fun ByteArray.readU32LE(offset: Int): Long {
    return (this[offset].toLong() and 0xFF) or
           ((this[offset + 1].toLong() and 0xFF) shl 8) or
           ((this[offset + 2].toLong() and 0xFF) shl 16) or
           ((this[offset + 3].toLong() and 0xFF) shl 24)
}

private fun ByteArray.readI64LE(offset: Int): Long {
    var result = 0L
    for (i in 0 until 8) {
        result = result or ((this[offset + i].toLong() and 0xFF) shl (i * 8))
    }
    return result
}

private fun ByteArray.readU64LE(offset: Int): BigInteger {
    val bytes = ByteArray(8)
    for (i in 0 until 8) {
        bytes[7 - i] = this[offset + i] // Reverse for BigInteger (big-endian)
    }
    return BigInteger(1, bytes) // 1 = positive
}

private fun ByteArray.readPubkey(offset: Int): PublicKey {
    return PublicKey(this.sliceArray(offset until offset + 32))
}

private fun ByteArray.readCandle(offset: Int): Candle {
    return Candle(
        open = this.readU32LE(offset),
        highD = this.readU16LE(offset + 4),
        lowD = this.readU16LE(offset + 6),
        closeD = this.readI16LE(offset + 8),
        volume = this.readU16LE(offset + 10)
    )
}

// ============================================================================
// Pool Parsing
// ============================================================================

/**
 * Parse Pool account data.
 *
 * @param data Raw account data (minimum 900 bytes)
 * @return Parsed Pool or null if invalid
 */
fun parsePool(data: ByteArray): Pool? {
    if (data.size < 900) return null

    val disc = data.sliceArray(0 until 8)
    if (!disc.contentEquals(AccountDiscriminators.POOL)) return null

    var offset = 8

    // Pubkeys (6 * 32 = 192 bytes)
    val authority = data.readPubkey(offset); offset += 32
    val mint0 = data.readPubkey(offset); offset += 32
    val mint1 = data.readPubkey(offset); offset += 32
    val vault0 = data.readPubkey(offset); offset += 32
    val vault1 = data.readPubkey(offset); offset += 32
    val lpMint = data.readPubkey(offset); offset += 32

    // Amp fields (5 * 8 = 40 bytes)
    val amp = data.readU64LE(offset); offset += 8
    val initAmp = data.readU64LE(offset); offset += 8
    val targetAmp = data.readU64LE(offset); offset += 8
    val rampStart = data.readI64LE(offset); offset += 8
    val rampStop = data.readI64LE(offset); offset += 8

    // Fee fields (2 * 8 = 16 bytes)
    val feeBps = data.readU64LE(offset); offset += 8
    val adminFeePct = data.readU64LE(offset); offset += 8

    // Balance fields (5 * 8 = 40 bytes)
    val bal0 = data.readU64LE(offset); offset += 8
    val bal1 = data.readU64LE(offset); offset += 8
    val lpSupply = data.readU64LE(offset); offset += 8
    val adminFee0 = data.readU64LE(offset); offset += 8
    val adminFee1 = data.readU64LE(offset); offset += 8

    // Volume fields (2 * 8 = 16 bytes)
    val vol0 = data.readU64LE(offset); offset += 8
    val vol1 = data.readU64LE(offset); offset += 8

    // Flags (5 bytes + 3 padding)
    val paused = data.readU8(offset) != 0; offset += 1
    val bump = data.readU8(offset); offset += 1
    val vault0Bump = data.readU8(offset); offset += 1
    val vault1Bump = data.readU8(offset); offset += 1
    val lpMintBump = data.readU8(offset); offset += 1
    offset += 3 // _pad[3]

    // Pending authority (32 + 8 = 40 bytes)
    val pendingAuth = data.readPubkey(offset); offset += 32
    val authTime = data.readI64LE(offset); offset += 8

    // Pending amp (8 + 8 = 16 bytes)
    val pendingAmp = data.readU64LE(offset); offset += 8
    val ampTime = data.readI64LE(offset); offset += 8

    // Analytics section
    val tradeCount = data.readU64LE(offset); offset += 8
    val tradeSum = data.readU64LE(offset); offset += 8
    val maxPrice = data.readU32LE(offset); offset += 4
    val minPrice = data.readU32LE(offset); offset += 4
    val hourSlot = data.readU32LE(offset); offset += 4
    val daySlot = data.readU32LE(offset); offset += 4
    val hourIdx = data.readU8(offset); offset += 1
    val dayIdx = data.readU8(offset); offset += 1
    offset += 6 // _pad2[6]

    // Bloom filter (128 bytes)
    val bloom = data.sliceArray(offset until offset + BLOOM_SIZE)
    offset += BLOOM_SIZE

    // Hourly candles (24 * 12 = 288 bytes)
    val hourlyCandles = mutableListOf<Candle>()
    for (i in 0 until OHLCV_24H) {
        hourlyCandles.add(data.readCandle(offset))
        offset += 12
    }

    // Daily candles (7 * 12 = 84 bytes)
    val dailyCandles = mutableListOf<Candle>()
    for (i in 0 until OHLCV_7D) {
        dailyCandles.add(data.readCandle(offset))
        offset += 12
    }

    return Pool(
        discriminator = disc,
        authority = authority,
        mint0 = mint0,
        mint1 = mint1,
        vault0 = vault0,
        vault1 = vault1,
        lpMint = lpMint,
        amp = amp,
        initAmp = initAmp,
        targetAmp = targetAmp,
        rampStart = rampStart,
        rampStop = rampStop,
        feeBps = feeBps,
        adminFeePct = adminFeePct,
        bal0 = bal0,
        bal1 = bal1,
        lpSupply = lpSupply,
        adminFee0 = adminFee0,
        adminFee1 = adminFee1,
        vol0 = vol0,
        vol1 = vol1,
        paused = paused,
        bump = bump,
        vault0Bump = vault0Bump,
        vault1Bump = vault1Bump,
        lpMintBump = lpMintBump,
        pendingAuth = pendingAuth,
        authTime = authTime,
        pendingAmp = pendingAmp,
        ampTime = ampTime,
        tradeCount = tradeCount,
        tradeSum = tradeSum,
        maxPrice = maxPrice,
        minPrice = minPrice,
        hourSlot = hourSlot,
        daySlot = daySlot,
        hourIdx = hourIdx,
        dayIdx = dayIdx,
        bloom = bloom,
        hourlyCandles = hourlyCandles,
        dailyCandles = dailyCandles
    )
}

// ============================================================================
// NPool Parsing
// ============================================================================

/**
 * Parse NPool account data.
 *
 * @param data Raw account data (minimum 800 bytes)
 * @return Parsed NPool or null if invalid
 */
fun parseNPool(data: ByteArray): NPool? {
    if (data.size < 800) return null

    val disc = data.sliceArray(0 until 8)
    if (!disc.contentEquals(AccountDiscriminators.NPOOL)) return null

    var offset = 8

    val authority = data.readPubkey(offset); offset += 32

    val nTokens = data.readU8(offset); offset += 1
    val paused = data.readU8(offset) != 0; offset += 1
    val bump = data.readU8(offset); offset += 1
    offset += 5 // _pad[5]

    val amp = data.readU64LE(offset); offset += 8
    val feeBps = data.readU64LE(offset); offset += 8
    val adminFeePct = data.readU64LE(offset); offset += 8
    val lpSupply = data.readU64LE(offset); offset += 8

    // Mints (8 * 32 = 256 bytes)
    val mints = mutableListOf<PublicKey>()
    for (i in 0 until MAX_TOKENS) {
        mints.add(data.readPubkey(offset))
        offset += 32
    }

    // Vaults (8 * 32 = 256 bytes)
    val vaults = mutableListOf<PublicKey>()
    for (i in 0 until MAX_TOKENS) {
        vaults.add(data.readPubkey(offset))
        offset += 32
    }

    val lpMint = data.readPubkey(offset); offset += 32

    // Balances (8 * 8 = 64 bytes)
    val balances = mutableListOf<BigInteger>()
    for (i in 0 until MAX_TOKENS) {
        balances.add(data.readU64LE(offset))
        offset += 8
    }

    // Admin fees (8 * 8 = 64 bytes)
    val adminFees = mutableListOf<BigInteger>()
    for (i in 0 until MAX_TOKENS) {
        adminFees.add(data.readU64LE(offset))
        offset += 8
    }

    val totalVolume = data.readU64LE(offset); offset += 8
    val tradeCount = data.readU64LE(offset); offset += 8
    val lastTradeSlot = data.readU64LE(offset); offset += 8

    return NPool(
        discriminator = disc,
        authority = authority,
        nTokens = nTokens,
        paused = paused,
        bump = bump,
        amp = amp,
        feeBps = feeBps,
        adminFeePct = adminFeePct,
        lpSupply = lpSupply,
        mints = mints.take(nTokens),
        vaults = vaults.take(nTokens),
        lpMint = lpMint,
        balances = balances.take(nTokens),
        adminFees = adminFees.take(nTokens),
        totalVolume = totalVolume,
        tradeCount = tradeCount,
        lastTradeSlot = lastTradeSlot
    )
}

// ============================================================================
// Farm Parsing
// ============================================================================

/**
 * Parse Farm account data.
 *
 * @param data Raw account data (minimum 120 bytes)
 * @return Parsed Farm or null if invalid
 */
fun parseFarm(data: ByteArray): Farm? {
    if (data.size < 120) return null

    val disc = data.sliceArray(0 until 8)
    if (!disc.contentEquals(AccountDiscriminators.FARM)) return null

    var offset = 8

    val pool = data.readPubkey(offset); offset += 32
    val rewardMint = data.readPubkey(offset); offset += 32
    val rewardRate = data.readU64LE(offset); offset += 8
    val startTime = data.readI64LE(offset); offset += 8
    val endTime = data.readI64LE(offset); offset += 8
    val totalStaked = data.readU64LE(offset); offset += 8
    val accReward = data.readU64LE(offset); offset += 8
    val lastUpdate = data.readI64LE(offset); offset += 8

    return Farm(
        discriminator = disc,
        pool = pool,
        rewardMint = rewardMint,
        rewardRate = rewardRate,
        startTime = startTime,
        endTime = endTime,
        totalStaked = totalStaked,
        accReward = accReward,
        lastUpdate = lastUpdate
    )
}

// ============================================================================
// UserFarm Parsing
// ============================================================================

/**
 * Parse UserFarm account data.
 *
 * @param data Raw account data (minimum 96 bytes)
 * @return Parsed UserFarm or null if invalid
 */
fun parseUserFarm(data: ByteArray): UserFarm? {
    if (data.size < 96) return null

    val disc = data.sliceArray(0 until 8)
    if (!disc.contentEquals(AccountDiscriminators.UFARM)) return null

    var offset = 8

    val owner = data.readPubkey(offset); offset += 32
    val farm = data.readPubkey(offset); offset += 32
    val staked = data.readU64LE(offset); offset += 8
    val rewardDebt = data.readU64LE(offset); offset += 8
    val lockEnd = data.readI64LE(offset); offset += 8

    return UserFarm(
        discriminator = disc,
        owner = owner,
        farm = farm,
        staked = staked,
        rewardDebt = rewardDebt,
        lockEnd = lockEnd
    )
}

// ============================================================================
// Lottery Parsing
// ============================================================================

/**
 * Parse Lottery account data.
 *
 * @param data Raw account data (minimum 152 bytes)
 * @return Parsed Lottery or null if invalid
 */
fun parseLottery(data: ByteArray): Lottery? {
    if (data.size < 152) return null

    val disc = data.sliceArray(0 until 8)
    if (!disc.contentEquals(AccountDiscriminators.LOTTERY)) return null

    var offset = 8

    val pool = data.readPubkey(offset); offset += 32
    val authority = data.readPubkey(offset); offset += 32
    val lotteryVault = data.readPubkey(offset); offset += 32
    val ticketPrice = data.readU64LE(offset); offset += 8
    val totalTickets = data.readU64LE(offset); offset += 8
    val prizePool = data.readU64LE(offset); offset += 8
    val endTime = data.readI64LE(offset); offset += 8
    val winningTicket = data.readU64LE(offset); offset += 8
    val drawn = data.readU8(offset) != 0; offset += 1
    val claimed = data.readU8(offset) != 0; offset += 1

    return Lottery(
        discriminator = disc,
        pool = pool,
        authority = authority,
        lotteryVault = lotteryVault,
        ticketPrice = ticketPrice,
        totalTickets = totalTickets,
        prizePool = prizePool,
        endTime = endTime,
        winningTicket = winningTicket,
        drawn = drawn,
        claimed = claimed
    )
}

// ============================================================================
// LotteryEntry Parsing
// ============================================================================

/**
 * Parse LotteryEntry account data.
 *
 * @param data Raw account data (minimum 88 bytes)
 * @return Parsed LotteryEntry or null if invalid
 */
fun parseLotteryEntry(data: ByteArray): LotteryEntry? {
    if (data.size < 88) return null

    val disc = data.sliceArray(0 until 8)
    if (!disc.contentEquals(AccountDiscriminators.LOTENTRY)) return null

    var offset = 8

    val owner = data.readPubkey(offset); offset += 32
    val lottery = data.readPubkey(offset); offset += 32
    val ticketStart = data.readU64LE(offset); offset += 8
    val ticketCount = data.readU64LE(offset); offset += 8

    return LotteryEntry(
        discriminator = disc,
        owner = owner,
        lottery = lottery,
        ticketStart = ticketStart,
        ticketCount = ticketCount
    )
}

// ============================================================================
// PDA Derivation
// ============================================================================

/**
 * Derive Pool PDA address.
 */
fun derivePoolPda(
    mint0: PublicKey,
    mint1: PublicKey,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    return PublicKey.findProgramAddress(
        listOf("pool".toByteArray(), mint0.toBytes(), mint1.toBytes()),
        programId
    )
}

/**
 * Derive Farm PDA address.
 */
fun deriveFarmPda(
    pool: PublicKey,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    return PublicKey.findProgramAddress(
        listOf("farm".toByteArray(), pool.toBytes()),
        programId
    )
}

/**
 * Derive UserFarm PDA address.
 */
fun deriveUserFarmPda(
    farm: PublicKey,
    user: PublicKey,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    return PublicKey.findProgramAddress(
        listOf("user_farm".toByteArray(), farm.toBytes(), user.toBytes()),
        programId
    )
}

/**
 * Derive Lottery PDA address.
 */
fun deriveLotteryPda(
    pool: PublicKey,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    return PublicKey.findProgramAddress(
        listOf("lottery".toByteArray(), pool.toBytes()),
        programId
    )
}

/**
 * Derive LotteryEntry PDA address.
 */
fun deriveLotteryEntryPda(
    lottery: PublicKey,
    user: PublicKey,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    return PublicKey.findProgramAddress(
        listOf("lottery_entry".toByteArray(), lottery.toBytes(), user.toBytes()),
        programId
    )
}

/**
 * Derive Global VPool PDA address.
 */
fun deriveGlobalVPoolPda(
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    return PublicKey.findProgramAddress(
        listOf("global_vpool".toByteArray()),
        programId
    )
}

/**
 * Derive VPool Mint PDA address.
 */
fun deriveVPoolMintPda(
    poolId: Int,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    val poolIdBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(poolId).array()
    return PublicKey.findProgramAddress(
        listOf("vpmint".toByteArray(), poolIdBytes),
        programId
    )
}

/**
 * Derive VPool Claim PDA address.
 */
fun deriveVPoolClaimPda(
    poolId: Int,
    wallet: PublicKey,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): Pair<PublicKey, Int> {
    val poolIdBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(poolId).array()
    return PublicKey.findProgramAddress(
        listOf("vpclaim".toByteArray(), poolIdBytes, wallet.toBytes()),
        programId
    )
}
