/**
 * AeX402 SDK - Data Types
 *
 * Data classes for account state, instruction arguments, and return types
 */
package com.aex402.sdk

import java.math.BigInteger

// ============================================================================
// Candle (12 bytes, delta-encoded OHLCV)
// ============================================================================

/**
 * Raw candle data as stored on-chain.
 * Uses delta encoding to save space.
 */
data class Candle(
    /** Base price (scaled 1e6) */
    val open: Long,
    /** High delta (high = open + highD) */
    val highD: Int,
    /** Low delta (low = open - lowD) */
    val lowD: Int,
    /** Close delta signed (close = open + closeD) */
    val closeD: Short,
    /** Volume in 1e9 units */
    val volume: Int
)

/**
 * Decoded candle with actual OHLCV values.
 */
data class CandleDecoded(
    val open: Long,
    val high: Long,
    val low: Long,
    val close: Long,
    val volume: Int
)

/**
 * Decode a raw candle to actual values.
 */
fun Candle.decode(): CandleDecoded = CandleDecoded(
    open = open,
    high = open + highD,
    low = open - lowD,
    close = open + closeD,
    volume = volume
)

// ============================================================================
// Pool (2-token) - matches C struct in aex402.c
// Size: 1024 bytes
// ============================================================================

/**
 * 2-token StableSwap pool with on-chain OHLCV analytics.
 */
data class Pool(
    /** 8 bytes "POOLSWAP" */
    val discriminator: ByteArray,
    /** Pool authority */
    val authority: PublicKey,
    /** Token 0 mint */
    val mint0: PublicKey,
    /** Token 1 mint */
    val mint1: PublicKey,
    /** Token 0 vault */
    val vault0: PublicKey,
    /** Token 1 vault */
    val vault1: PublicKey,
    /** LP token mint */
    val lpMint: PublicKey,
    /** Current amplification */
    val amp: BigInteger,
    /** Initial amp for ramping */
    val initAmp: BigInteger,
    /** Target amp for ramping */
    val targetAmp: BigInteger,
    /** Ramp start timestamp */
    val rampStart: Long,
    /** Ramp stop timestamp */
    val rampStop: Long,
    /** Swap fee in basis points */
    val feeBps: BigInteger,
    /** Admin fee percentage (0-100) */
    val adminFeePct: BigInteger,
    /** Token 0 balance */
    val bal0: BigInteger,
    /** Token 1 balance */
    val bal1: BigInteger,
    /** Total LP supply */
    val lpSupply: BigInteger,
    /** Admin fee 0 */
    val adminFee0: BigInteger,
    /** Admin fee 1 */
    val adminFee1: BigInteger,
    /** Volume token 0 */
    val vol0: BigInteger,
    /** Volume token 1 */
    val vol1: BigInteger,
    /** Pool paused flag */
    val paused: Boolean,
    /** Pool PDA bump */
    val bump: Int,
    /** Vault 0 bump */
    val vault0Bump: Int,
    /** Vault 1 bump */
    val vault1Bump: Int,
    /** LP mint bump */
    val lpMintBump: Int,
    /** Pending authority for transfer */
    val pendingAuth: PublicKey,
    /** Authority transfer timestamp */
    val authTime: Long,
    /** Pending amp change */
    val pendingAmp: BigInteger,
    /** Amp change timestamp */
    val ampTime: Long,
    /** Trade count */
    val tradeCount: BigInteger,
    /** Sum of trades */
    val tradeSum: BigInteger,
    /** Max price seen */
    val maxPrice: Long,
    /** Min price seen */
    val minPrice: Long,
    /** Current hour slot */
    val hourSlot: Long,
    /** Current day slot */
    val daySlot: Long,
    /** Hourly candle index */
    val hourIdx: Int,
    /** Daily candle index */
    val dayIdx: Int,
    /** Bloom filter (reserved) */
    val bloom: ByteArray,
    /** 24 hourly candles */
    val hourlyCandles: List<Candle>,
    /** 7 daily candles */
    val dailyCandles: List<Candle>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Pool
        return discriminator.contentEquals(other.discriminator) &&
               authority == other.authority &&
               mint0 == other.mint0
    }

    override fun hashCode(): Int {
        var result = discriminator.contentHashCode()
        result = 31 * result + authority.hashCode()
        result = 31 * result + mint0.hashCode()
        return result
    }
}

// ============================================================================
// NPool (N-token, 2-8 tokens) - matches C struct in aex402.c
// Size: 2048 bytes
// ============================================================================

/**
 * N-token pool supporting 2-8 tokens.
 */
data class NPool(
    /** 8 bytes "NPOOLSWA" */
    val discriminator: ByteArray,
    /** Pool authority */
    val authority: PublicKey,
    /** Number of tokens (2-8) */
    val nTokens: Int,
    /** Pool paused flag */
    val paused: Boolean,
    /** Pool PDA bump */
    val bump: Int,
    /** Amplification coefficient */
    val amp: BigInteger,
    /** Swap fee in basis points */
    val feeBps: BigInteger,
    /** Admin fee percentage */
    val adminFeePct: BigInteger,
    /** Total LP supply */
    val lpSupply: BigInteger,
    /** Token mints (up to 8) */
    val mints: List<PublicKey>,
    /** Token vaults (up to 8) */
    val vaults: List<PublicKey>,
    /** LP token mint */
    val lpMint: PublicKey,
    /** Token balances (up to 8) */
    val balances: List<BigInteger>,
    /** Admin fees (up to 8) */
    val adminFees: List<BigInteger>,
    /** Total trading volume */
    val totalVolume: BigInteger,
    /** Trade count */
    val tradeCount: BigInteger,
    /** Last trade slot */
    val lastTradeSlot: BigInteger
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NPool
        return discriminator.contentEquals(other.discriminator) && authority == other.authority
    }

    override fun hashCode(): Int {
        var result = discriminator.contentHashCode()
        result = 31 * result + authority.hashCode()
        return result
    }
}

// ============================================================================
// Lottery - matches C struct in aex402.c
// ============================================================================

/**
 * LP-based lottery for a pool.
 */
data class Lottery(
    /** 8 bytes "LOTTERY!" */
    val discriminator: ByteArray,
    /** Associated pool */
    val pool: PublicKey,
    /** Lottery authority */
    val authority: PublicKey,
    /** Lottery vault for LP tokens */
    val lotteryVault: PublicKey,
    /** Ticket price in LP tokens */
    val ticketPrice: BigInteger,
    /** Total tickets sold */
    val totalTickets: BigInteger,
    /** Total prize pool */
    val prizePool: BigInteger,
    /** End timestamp */
    val endTime: Long,
    /** Winning ticket number */
    val winningTicket: BigInteger,
    /** Whether drawn */
    val drawn: Boolean,
    /** Whether prize claimed */
    val claimed: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Lottery
        return discriminator.contentEquals(other.discriminator) && pool == other.pool
    }

    override fun hashCode(): Int = discriminator.contentHashCode() + pool.hashCode()
}

// ============================================================================
// LotteryEntry - matches C struct in aex402.c
// ============================================================================

/**
 * User's entry in a lottery.
 */
data class LotteryEntry(
    /** 8 bytes "LOTENTRY" */
    val discriminator: ByteArray,
    /** Entry owner */
    val owner: PublicKey,
    /** Associated lottery */
    val lottery: PublicKey,
    /** Starting ticket number */
    val ticketStart: BigInteger,
    /** Number of tickets */
    val ticketCount: BigInteger
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LotteryEntry
        return discriminator.contentEquals(other.discriminator) && owner == other.owner
    }

    override fun hashCode(): Int = discriminator.contentHashCode() + owner.hashCode()
}

// ============================================================================
// Farm - matches C struct in aex402.c
// ============================================================================

/**
 * Farming pool for LP staking rewards.
 */
data class Farm(
    /** 8 bytes "FARMSWAP" */
    val discriminator: ByteArray,
    /** Associated pool */
    val pool: PublicKey,
    /** Reward token mint */
    val rewardMint: PublicKey,
    /** Rewards per second */
    val rewardRate: BigInteger,
    /** Farming start time */
    val startTime: Long,
    /** Farming end time */
    val endTime: Long,
    /** Total LP staked */
    val totalStaked: BigInteger,
    /** Accumulated reward per share (scaled 1e12) */
    val accReward: BigInteger,
    /** Last update timestamp */
    val lastUpdate: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Farm
        return discriminator.contentEquals(other.discriminator) && pool == other.pool
    }

    override fun hashCode(): Int = discriminator.contentHashCode() + pool.hashCode()
}

// ============================================================================
// UserFarm - matches C struct in aex402.c
// ============================================================================

/**
 * User's farming position.
 */
data class UserFarm(
    /** 8 bytes "UFARMSWA" */
    val discriminator: ByteArray,
    /** Position owner */
    val owner: PublicKey,
    /** Associated farm */
    val farm: PublicKey,
    /** LP tokens staked */
    val staked: BigInteger,
    /** Reward debt (for accumulated reward calculation) */
    val rewardDebt: BigInteger,
    /** Lock end timestamp (0 = not locked) */
    val lockEnd: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserFarm
        return discriminator.contentEquals(other.discriminator) && owner == other.owner
    }

    override fun hashCode(): Int = discriminator.contentHashCode() + owner.hashCode()
}

// ============================================================================
// Registry - for pool enumeration
// ============================================================================

/**
 * Pool registry for on-chain enumeration.
 */
data class Registry(
    val discriminator: ByteArray,
    val authority: PublicKey,
    val pendingAuth: PublicKey,
    val authTime: Long,
    val count: Int,
    val pools: List<PublicKey>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Registry
        return discriminator.contentEquals(other.discriminator)
    }

    override fun hashCode(): Int = discriminator.contentHashCode()
}

// ============================================================================
// TWAP Result
// ============================================================================

/**
 * TWAP oracle result.
 */
data class TwapResult(
    /** Price scaled 1e6 */
    val price: Long,
    /** Number of candles used */
    val samples: Int,
    /** Confidence 0-10000 (0-100%) */
    val confidence: Int
) {
    /**
     * Get price as a floating point number.
     */
    fun priceAsFloat(): Double = price / 1_000_000.0

    /**
     * Get confidence as a percentage (0-100).
     */
    fun confidencePercent(): Double = confidence / 100.0
}

/**
 * Decode TWAP result from u64 return value.
 */
fun decodeTwapResult(encoded: BigInteger): TwapResult {
    val bytes = encoded.toByteArray().let { raw ->
        // Pad or trim to 8 bytes, little-endian
        ByteArray(8).also { padded ->
            val start = maxOf(0, raw.size - 8)
            val destStart = maxOf(0, 8 - raw.size)
            raw.copyInto(padded, destStart, start, raw.size)
        }
    }
    // Reverse for little-endian
    bytes.reverse()

    val price = (bytes[0].toLong() and 0xFF) or
            ((bytes[1].toLong() and 0xFF) shl 8) or
            ((bytes[2].toLong() and 0xFF) shl 16) or
            ((bytes[3].toLong() and 0xFF) shl 24)
    val samples = (bytes[4].toInt() and 0xFF) or ((bytes[5].toInt() and 0xFF) shl 8)
    val confidence = (bytes[6].toInt() and 0xFF) or ((bytes[7].toInt() and 0xFF) shl 8)

    return TwapResult(price, samples, confidence)
}

// ============================================================================
// Instruction Arguments
// ============================================================================

data class CreatePoolArgs(
    val amp: BigInteger,
    val bump: Int
)

data class CreateNPoolArgs(
    val amp: BigInteger,
    val nTokens: Int,
    val bump: Int
)

data class SwapArgs(
    val from: Int,
    val to: Int,
    val amountIn: BigInteger,
    val minOut: BigInteger,
    val deadline: Long
)

data class SwapSimpleArgs(
    val amountIn: BigInteger,
    val minOut: BigInteger
)

data class SwapNArgs(
    val fromIdx: Int,
    val toIdx: Int,
    val amountIn: BigInteger,
    val minOut: BigInteger
)

data class AddLiqArgs(
    val amount0: BigInteger,
    val amount1: BigInteger,
    val minLp: BigInteger
)

data class AddLiq1Args(
    val amountIn: BigInteger,
    val minLp: BigInteger
)

data class RemLiqArgs(
    val lpAmount: BigInteger,
    val min0: BigInteger,
    val min1: BigInteger
)

data class UpdateFeeArgs(
    val feeBps: BigInteger
)

data class CommitAmpArgs(
    val targetAmp: BigInteger
)

data class RampAmpArgs(
    val targetAmp: BigInteger,
    val duration: Long
)

data class CreateFarmArgs(
    val rewardRate: BigInteger,
    val startTime: Long,
    val endTime: Long
)

data class StakeArgs(
    val amount: BigInteger
)

data class LockLpArgs(
    val amount: BigInteger,
    val duration: Long
)

data class CreateLotteryArgs(
    val ticketPrice: BigInteger,
    val endTime: Long
)

data class EnterLotteryArgs(
    val ticketCount: BigInteger
)

data class DrawLotteryArgs(
    val randomSeed: BigInteger
)

// ============================================================================
// Virtual Pool Types
// ============================================================================

/**
 * Slot status for virtual pools.
 */
enum class SlotStatus(val value: Int) {
    FREE(0),
    ACTIVE(1),
    GRADUATED(2),
    FLUSHED(3);

    companion object {
        fun fromValue(value: Int): SlotStatus = entries.find { it.value == value } ?: FREE
    }
}

/**
 * Global header for virtual pool PDA.
 */
data class GlobalHeader(
    val discriminator: ByteArray,
    val numSlots: Int,
    val nextPoolId: Int,
    val feeBalance: BigInteger,
    val totalVolume: BigInteger,
    val activePools: Int,
    val graduatedCount: Int,
    val flushedCount: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GlobalHeader
        return discriminator.contentEquals(other.discriminator)
    }

    override fun hashCode(): Int = discriminator.contentHashCode()
}

/**
 * Virtual pool slot.
 */
data class VPoolSlot(
    val slotIndex: Int,
    val status: SlotStatus,
    val poolId: Int,
    val creator: PublicKey,
    val name: String,
    val symbol: String,
    val uri: String,
    val basePrice: BigInteger,
    val slope: BigInteger,
    val totalSupply: BigInteger,
    val tokensSold: BigInteger,
    val solRaised: BigInteger,
    val totalBuySol: BigInteger,
    val totalSellSol: BigInteger,
    val createdAt: Long,
    val lastTradeAt: Long,
    val realPool: PublicKey,
    val realMint: PublicKey,
    val creatorUnclaimed: BigInteger,
    val creatorLastClaim: Long,
    val graduationTriggerer: PublicKey,
    val holderCount: Int,
    val mintBump: Int,
    val hashPositions: List<Int>
)

/**
 * Virtual pool holder entry.
 */
data class VPoolHolder(
    val walletHash: ByteArray,
    val balance: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VPoolHolder
        return walletHash.contentEquals(other.walletHash)
    }

    override fun hashCode(): Int = walletHash.contentHashCode()
}

/**
 * Virtual pool claim PDA.
 */
data class VPoolClaimPDA(
    val discriminator: ByteArray,
    val poolId: Int,
    val wallet: PublicKey,
    val unclaimed: BigInteger,
    val claimed: BigInteger,
    val lastClaim: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VPoolClaimPDA
        return discriminator.contentEquals(other.discriminator) && wallet == other.wallet
    }

    override fun hashCode(): Int = discriminator.contentHashCode() + wallet.hashCode()
}

/**
 * Virtual pool statistics.
 */
data class VPoolStats(
    val currentPrice: BigInteger,
    val marketCapSol: BigInteger,
    val graduationProgress: Double,
    val canGraduate: Boolean,
    val graduationTarget: BigInteger,
    val churnRatio: Double,
    val ageSeconds: Long,
    val isStale: Boolean
)

/**
 * Buy simulation result.
 */
data class BuySimulation(
    val tokensOut: BigInteger,
    val newPrice: BigInteger,
    val priceImpact: Double,
    val fee: BigInteger
)

/**
 * Sell simulation result.
 */
data class SellSimulation(
    val solOut: BigInteger,
    val newPrice: BigInteger,
    val priceImpact: Double,
    val fee: BigInteger
)
