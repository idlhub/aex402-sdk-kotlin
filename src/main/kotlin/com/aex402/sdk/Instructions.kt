/**
 * AeX402 SDK - Instruction Builders
 *
 * Functions to build transaction instructions for all AeX402 handlers.
 */
package com.aex402.sdk

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

// ============================================================================
// Buffer Writing Helpers
// ============================================================================

private fun ByteBuffer.putU8(value: Int): ByteBuffer = put(value.toByte())

private fun ByteBuffer.putU64(value: BigInteger): ByteBuffer {
    val bytes = value.toByteArray()
    val buffer = ByteArray(8)
    // Copy to little-endian format
    val start = maxOf(0, bytes.size - 8)
    val destStart = 0
    for (i in start until bytes.size) {
        buffer[bytes.size - 1 - i] = bytes[i]
    }
    return put(buffer)
}

private fun ByteBuffer.putI64(value: Long): ByteBuffer = putLong(value)

private fun allocateBuffer(size: Int): ByteBuffer =
    ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)

// ============================================================================
// Program ID
// ============================================================================

private val PROGRAM = PublicKey(PROGRAM_ID)
private val SYSTEM = PublicKey.SYSTEM_PROGRAM
private val TOKEN = PublicKey.TOKEN_PROGRAM

// ============================================================================
// Pool Creation Instructions
// ============================================================================

/**
 * Create a 2-token pool.
 */
fun createPoolInstruction(
    pool: PublicKey,
    mint0: PublicKey,
    mint1: PublicKey,
    authority: PublicKey,
    args: CreatePoolArgs
): TransactionInstruction {
    val data = allocateBuffer(17)
    data.put(Discriminators.CREATEPOOL)
    data.putU64(args.amp)
    data.putU8(args.bump)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(mint0, isSigner = false, isWritable = false),
            AccountMeta(mint1, isSigner = false, isWritable = false),
            AccountMeta(authority, isSigner = true, isWritable = true),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Initialize Token 0 vault.
 */
fun initT0VaultInstruction(
    pool: PublicKey,
    vault: PublicKey,
    authority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault, isSigner = false, isWritable = false),
            AccountMeta(authority, isSigner = true, isWritable = false),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = Discriminators.INITT0V
    )
}

/**
 * Initialize Token 1 vault.
 */
fun initT1VaultInstruction(
    pool: PublicKey,
    vault: PublicKey,
    authority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault, isSigner = false, isWritable = false),
            AccountMeta(authority, isSigner = true, isWritable = false),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = Discriminators.INITT1V
    )
}

/**
 * Initialize LP mint.
 */
fun initLpMintInstruction(
    pool: PublicKey,
    lpMint: PublicKey,
    authority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(lpMint, isSigner = false, isWritable = false),
            AccountMeta(authority, isSigner = true, isWritable = false),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = Discriminators.INITLPM
    )
}

// ============================================================================
// Swap Instructions
// ============================================================================

/**
 * Generic swap instruction.
 */
fun swapInstruction(
    pool: PublicKey,
    vault0: PublicKey,
    vault1: PublicKey,
    userToken0: PublicKey,
    userToken1: PublicKey,
    user: PublicKey,
    args: SwapArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(34)
    data.put(Discriminators.SWAP)
    data.putU8(args.from)
    data.putU8(args.to)
    data.putU64(args.amountIn)
    data.putU64(args.minOut)
    data.putI64(args.deadline)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault0, isSigner = false, isWritable = true),
            AccountMeta(vault1, isSigner = false, isWritable = true),
            AccountMeta(userToken0, isSigner = false, isWritable = true),
            AccountMeta(userToken1, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Swap Token 0 to Token 1.
 */
fun swapT0T1Instruction(
    pool: PublicKey,
    vault0: PublicKey,
    vault1: PublicKey,
    userToken0: PublicKey,
    userToken1: PublicKey,
    user: PublicKey,
    args: SwapSimpleArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(24)
    data.put(Discriminators.SWAPT0T1)
    data.putU64(args.amountIn)
    data.putU64(args.minOut)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault0, isSigner = false, isWritable = true),
            AccountMeta(vault1, isSigner = false, isWritable = true),
            AccountMeta(userToken0, isSigner = false, isWritable = true),
            AccountMeta(userToken1, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Swap Token 1 to Token 0.
 */
fun swapT1T0Instruction(
    pool: PublicKey,
    vault0: PublicKey,
    vault1: PublicKey,
    userToken0: PublicKey,
    userToken1: PublicKey,
    user: PublicKey,
    args: SwapSimpleArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(24)
    data.put(Discriminators.SWAPT1T0)
    data.putU64(args.amountIn)
    data.putU64(args.minOut)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault0, isSigner = false, isWritable = true),
            AccountMeta(vault1, isSigner = false, isWritable = true),
            AccountMeta(userToken0, isSigner = false, isWritable = true),
            AccountMeta(userToken1, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * N-token pool swap.
 */
fun swapNInstruction(
    pool: PublicKey,
    vaultIn: PublicKey,
    vaultOut: PublicKey,
    userTokenIn: PublicKey,
    userTokenOut: PublicKey,
    user: PublicKey,
    args: SwapNArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(26)
    data.put(Discriminators.SWAPN)
    data.putU8(args.fromIdx)
    data.putU8(args.toIdx)
    data.putU64(args.amountIn)
    data.putU64(args.minOut)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vaultIn, isSigner = false, isWritable = true),
            AccountMeta(vaultOut, isSigner = false, isWritable = true),
            AccountMeta(userTokenIn, isSigner = false, isWritable = true),
            AccountMeta(userTokenOut, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

// ============================================================================
// Liquidity Instructions
// ============================================================================

/**
 * Add liquidity to 2-token pool.
 */
fun addLiquidityInstruction(
    pool: PublicKey,
    vault0: PublicKey,
    vault1: PublicKey,
    lpMint: PublicKey,
    userToken0: PublicKey,
    userToken1: PublicKey,
    userLp: PublicKey,
    user: PublicKey,
    args: AddLiqArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(32)
    data.put(Discriminators.ADDLIQ)
    data.putU64(args.amount0)
    data.putU64(args.amount1)
    data.putU64(args.minLp)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault0, isSigner = false, isWritable = true),
            AccountMeta(vault1, isSigner = false, isWritable = true),
            AccountMeta(lpMint, isSigner = false, isWritable = true),
            AccountMeta(userToken0, isSigner = false, isWritable = true),
            AccountMeta(userToken1, isSigner = false, isWritable = true),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Single-sided add liquidity.
 */
fun addLiquidity1Instruction(
    pool: PublicKey,
    vaultIn: PublicKey,
    lpMint: PublicKey,
    userTokenIn: PublicKey,
    userLp: PublicKey,
    user: PublicKey,
    args: AddLiq1Args,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(24)
    data.put(Discriminators.ADDLIQ1)
    data.putU64(args.amountIn)
    data.putU64(args.minLp)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vaultIn, isSigner = false, isWritable = true),
            AccountMeta(lpMint, isSigner = false, isWritable = true),
            AccountMeta(userTokenIn, isSigner = false, isWritable = true),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Remove liquidity from 2-token pool.
 */
fun removeLiquidityInstruction(
    pool: PublicKey,
    vault0: PublicKey,
    vault1: PublicKey,
    lpMint: PublicKey,
    userToken0: PublicKey,
    userToken1: PublicKey,
    userLp: PublicKey,
    user: PublicKey,
    args: RemLiqArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(32)
    data.put(Discriminators.REMLIQ)
    data.putU64(args.lpAmount)
    data.putU64(args.min0)
    data.putU64(args.min1)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault0, isSigner = false, isWritable = true),
            AccountMeta(vault1, isSigner = false, isWritable = true),
            AccountMeta(lpMint, isSigner = false, isWritable = true),
            AccountMeta(userToken0, isSigner = false, isWritable = true),
            AccountMeta(userToken1, isSigner = false, isWritable = true),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

// ============================================================================
// Admin Instructions
// ============================================================================

/**
 * Set pool pause state.
 */
fun setPauseInstruction(
    pool: PublicKey,
    authority: PublicKey,
    paused: Boolean
): TransactionInstruction {
    val data = allocateBuffer(9)
    data.put(Discriminators.SETPAUSE)
    data.putU8(if (paused) 1 else 0)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Update swap fee.
 */
fun updateFeeInstruction(
    pool: PublicKey,
    authority: PublicKey,
    args: UpdateFeeArgs
): TransactionInstruction {
    val data = allocateBuffer(16)
    data.put(Discriminators.UPDFEE)
    data.putU64(args.feeBps)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Withdraw admin fees.
 */
fun withdrawFeeInstruction(
    pool: PublicKey,
    vault0: PublicKey,
    vault1: PublicKey,
    dest0: PublicKey,
    dest1: PublicKey,
    authority: PublicKey,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(vault0, isSigner = false, isWritable = true),
            AccountMeta(vault1, isSigner = false, isWritable = true),
            AccountMeta(dest0, isSigner = false, isWritable = true),
            AccountMeta(dest1, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = Discriminators.WDRAWFEE
    )
}

/**
 * Commit amp change (timelock).
 */
fun commitAmpInstruction(
    pool: PublicKey,
    authority: PublicKey,
    args: CommitAmpArgs
): TransactionInstruction {
    val data = allocateBuffer(16)
    data.put(Discriminators.COMMITAMP)
    data.putU64(args.targetAmp)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Start amp ramping.
 */
fun rampAmpInstruction(
    pool: PublicKey,
    authority: PublicKey,
    args: RampAmpArgs
): TransactionInstruction {
    val data = allocateBuffer(24)
    data.put(Discriminators.RAMPAMP)
    data.putU64(args.targetAmp)
    data.putI64(args.duration)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Stop amp ramping.
 */
fun stopRampInstruction(
    pool: PublicKey,
    authority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false)
        ),
        data = Discriminators.STOPRAMP
    )
}

/**
 * Initiate authority transfer.
 */
fun initAuthTransferInstruction(
    pool: PublicKey,
    authority: PublicKey,
    newAuthority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false),
            AccountMeta(newAuthority, isSigner = false, isWritable = false)
        ),
        data = Discriminators.INITAUTH
    )
}

/**
 * Complete authority transfer.
 */
fun completeAuthTransferInstruction(
    pool: PublicKey,
    newAuthority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(newAuthority, isSigner = true, isWritable = false)
        ),
        data = Discriminators.COMPLAUTH
    )
}

/**
 * Cancel authority transfer.
 */
fun cancelAuthTransferInstruction(
    pool: PublicKey,
    authority: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false)
        ),
        data = Discriminators.CANCELAUTH
    )
}

// ============================================================================
// Farming Instructions
// ============================================================================

/**
 * Create a farming period.
 */
fun createFarmInstruction(
    farm: PublicKey,
    pool: PublicKey,
    rewardMint: PublicKey,
    authority: PublicKey,
    args: CreateFarmArgs
): TransactionInstruction {
    val data = allocateBuffer(32)
    data.put(Discriminators.CREATEFARM)
    data.putU64(args.rewardRate)
    data.putI64(args.startTime)
    data.putI64(args.endTime)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(farm, isSigner = false, isWritable = true),
            AccountMeta(pool, isSigner = false, isWritable = false),
            AccountMeta(rewardMint, isSigner = false, isWritable = false),
            AccountMeta(authority, isSigner = true, isWritable = true),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Stake LP tokens.
 */
fun stakeLpInstruction(
    userPosition: PublicKey,
    farm: PublicKey,
    userLp: PublicKey,
    lpVault: PublicKey,
    user: PublicKey,
    args: StakeArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(16)
    data.put(Discriminators.STAKELP)
    data.putU64(args.amount)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(userPosition, isSigner = false, isWritable = true),
            AccountMeta(farm, isSigner = false, isWritable = true),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(lpVault, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Unstake LP tokens.
 */
fun unstakeLpInstruction(
    userPosition: PublicKey,
    farm: PublicKey,
    userLp: PublicKey,
    lpVault: PublicKey,
    user: PublicKey,
    args: StakeArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(16)
    data.put(Discriminators.UNSTAKELP)
    data.putU64(args.amount)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(userPosition, isSigner = false, isWritable = true),
            AccountMeta(farm, isSigner = false, isWritable = true),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(lpVault, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Claim farming rewards.
 */
fun claimFarmInstruction(
    userPosition: PublicKey,
    farm: PublicKey,
    pool: PublicKey,
    rewardVault: PublicKey,
    userReward: PublicKey,
    user: PublicKey,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(userPosition, isSigner = false, isWritable = true),
            AccountMeta(farm, isSigner = false, isWritable = true),
            AccountMeta(pool, isSigner = false, isWritable = false),
            AccountMeta(rewardVault, isSigner = false, isWritable = true),
            AccountMeta(userReward, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = Discriminators.CLAIMFARM
    )
}

/**
 * Lock LP tokens.
 */
fun lockLpInstruction(
    userPosition: PublicKey,
    farm: PublicKey,
    user: PublicKey,
    args: LockLpArgs
): TransactionInstruction {
    val data = allocateBuffer(24)
    data.put(Discriminators.LOCKLP)
    data.putU64(args.amount)
    data.putI64(args.duration)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(userPosition, isSigner = false, isWritable = true),
            AccountMeta(farm, isSigner = false, isWritable = false),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Claim unlocked LP tokens.
 */
fun claimUnlockedLpInstruction(
    userPosition: PublicKey,
    farm: PublicKey,
    user: PublicKey
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(userPosition, isSigner = false, isWritable = true),
            AccountMeta(farm, isSigner = false, isWritable = false),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = Discriminators.CLAIMULP
    )
}

// ============================================================================
// Lottery Instructions
// ============================================================================

/**
 * Create a lottery.
 */
fun createLotteryInstruction(
    lottery: PublicKey,
    pool: PublicKey,
    lotteryVault: PublicKey,
    authority: PublicKey,
    args: CreateLotteryArgs
): TransactionInstruction {
    val data = allocateBuffer(24)
    data.put(Discriminators.CREATELOT)
    data.putU64(args.ticketPrice)
    data.putI64(args.endTime)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(lottery, isSigner = false, isWritable = true),
            AccountMeta(pool, isSigner = false, isWritable = false),
            AccountMeta(lotteryVault, isSigner = false, isWritable = false),
            AccountMeta(authority, isSigner = true, isWritable = true),
            AccountMeta(SYSTEM, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Enter a lottery.
 */
fun enterLotteryInstruction(
    lottery: PublicKey,
    userEntry: PublicKey,
    user: PublicKey,
    userLp: PublicKey,
    lotteryVault: PublicKey,
    args: EnterLotteryArgs,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    val data = allocateBuffer(16)
    data.put(Discriminators.ENTERLOT)
    data.putU64(args.ticketCount)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(lottery, isSigner = false, isWritable = true),
            AccountMeta(userEntry, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(lotteryVault, isSigner = false, isWritable = true),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Draw lottery winner.
 */
fun drawLotteryInstruction(
    lottery: PublicKey,
    authority: PublicKey,
    recentSlothashes: PublicKey,
    args: DrawLotteryArgs
): TransactionInstruction {
    val data = allocateBuffer(16)
    data.put(Discriminators.DRAWLOT)
    data.putU64(args.randomSeed)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(lottery, isSigner = false, isWritable = true),
            AccountMeta(authority, isSigner = true, isWritable = false),
            AccountMeta(recentSlothashes, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}

/**
 * Claim lottery prize.
 */
fun claimLotteryInstruction(
    lottery: PublicKey,
    userEntry: PublicKey,
    user: PublicKey,
    userLp: PublicKey,
    lotteryVault: PublicKey,
    pool: PublicKey,
    tokenProgram: PublicKey = TOKEN
): TransactionInstruction {
    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(lottery, isSigner = false, isWritable = true),
            AccountMeta(userEntry, isSigner = false, isWritable = true),
            AccountMeta(user, isSigner = true, isWritable = false),
            AccountMeta(userLp, isSigner = false, isWritable = true),
            AccountMeta(lotteryVault, isSigner = false, isWritable = true),
            AccountMeta(pool, isSigner = false, isWritable = false),
            AccountMeta(tokenProgram, isSigner = false, isWritable = false)
        ),
        data = Discriminators.CLAIMLOT
    )
}

// ============================================================================
// TWAP Oracle Instruction
// ============================================================================

/**
 * Get TWAP oracle price.
 */
fun getTwapInstruction(
    pool: PublicKey,
    window: TwapWindow
): TransactionInstruction {
    val data = allocateBuffer(9)
    data.put(Discriminators.GETTWAP)
    data.putU8(window.value)

    return TransactionInstruction(
        programId = PROGRAM,
        keys = listOf(
            AccountMeta(pool, isSigner = false, isWritable = false)
        ),
        data = data.array()
    )
}
