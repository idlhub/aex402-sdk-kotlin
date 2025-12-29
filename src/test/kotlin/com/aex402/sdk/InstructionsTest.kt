package com.aex402.sdk

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigInteger

class InstructionsTest {

    private val testPool = PublicKey(ByteArray(32) { 1 })
    private val testVault0 = PublicKey(ByteArray(32) { 2 })
    private val testVault1 = PublicKey(ByteArray(32) { 3 })
    private val testUser = PublicKey(ByteArray(32) { 4 })
    private val testUserToken0 = PublicKey(ByteArray(32) { 5 })
    private val testUserToken1 = PublicKey(ByteArray(32) { 6 })
    private val testLpMint = PublicKey(ByteArray(32) { 7 })
    private val testUserLp = PublicKey(ByteArray(32) { 8 })

    @Test
    fun `createPoolInstruction has correct structure`() {
        val mint0 = PublicKey(ByteArray(32) { 10 })
        val mint1 = PublicKey(ByteArray(32) { 11 })
        val authority = PublicKey(ByteArray(32) { 12 })

        val ix = createPoolInstruction(
            testPool, mint0, mint1, authority,
            CreatePoolArgs(amp = BigInteger.valueOf(100), bump = 42)
        )

        assertEquals(PublicKey(PROGRAM_ID), ix.programId)
        assertEquals(5, ix.keys.size)

        // Check account order and flags
        assertTrue(ix.keys[0].isWritable)    // pool
        assertFalse(ix.keys[1].isWritable)   // mint0
        assertFalse(ix.keys[2].isWritable)   // mint1
        assertTrue(ix.keys[3].isSigner)      // authority
        assertFalse(ix.keys[4].isWritable)   // system program

        // Check data starts with discriminator
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.CREATEPOOL))
        assertEquals(17, ix.data.size)  // 8 (disc) + 8 (amp) + 1 (bump)
    }

    @Test
    fun `swapT0T1Instruction has correct structure`() {
        val ix = swapT0T1Instruction(
            testPool, testVault0, testVault1,
            testUserToken0, testUserToken1, testUser,
            SwapSimpleArgs(
                amountIn = BigInteger.valueOf(1_000_000_000),
                minOut = BigInteger.valueOf(990_000_000)
            )
        )

        assertEquals(PublicKey(PROGRAM_ID), ix.programId)
        assertEquals(7, ix.keys.size)

        // Check data
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.SWAPT0T1))
        assertEquals(24, ix.data.size)  // 8 (disc) + 8 (amountIn) + 8 (minOut)
    }

    @Test
    fun `swapInstruction includes all parameters`() {
        val ix = swapInstruction(
            testPool, testVault0, testVault1,
            testUserToken0, testUserToken1, testUser,
            SwapArgs(
                from = 0, to = 1,
                amountIn = BigInteger.valueOf(1_000_000_000),
                minOut = BigInteger.valueOf(990_000_000),
                deadline = 1704153600
            )
        )

        assertEquals(34, ix.data.size)  // 8 + 1 + 1 + 8 + 8 + 8
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.SWAP))
    }

    @Test
    fun `addLiquidityInstruction has correct structure`() {
        val ix = addLiquidityInstruction(
            testPool, testVault0, testVault1, testLpMint,
            testUserToken0, testUserToken1, testUserLp, testUser,
            AddLiqArgs(
                amount0 = BigInteger.valueOf(1_000_000_000),
                amount1 = BigInteger.valueOf(1_000_000_000),
                minLp = BigInteger.valueOf(900_000_000)
            )
        )

        assertEquals(9, ix.keys.size)
        assertEquals(32, ix.data.size)  // 8 + 8 + 8 + 8
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.ADDLIQ))
    }

    @Test
    fun `removeLiquidityInstruction has correct structure`() {
        val ix = removeLiquidityInstruction(
            testPool, testVault0, testVault1, testLpMint,
            testUserToken0, testUserToken1, testUserLp, testUser,
            RemLiqArgs(
                lpAmount = BigInteger.valueOf(1_000_000_000),
                min0 = BigInteger.valueOf(900_000_000),
                min1 = BigInteger.valueOf(900_000_000)
            )
        )

        assertEquals(9, ix.keys.size)
        assertEquals(32, ix.data.size)
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.REMLIQ))
    }

    @Test
    fun `setPauseInstruction sets correct flag`() {
        val authority = PublicKey(ByteArray(32) { 12 })

        val pauseIx = setPauseInstruction(testPool, authority, paused = true)
        assertEquals(9, pauseIx.data.size)
        assertEquals(1.toByte(), pauseIx.data[8])

        val unpauseIx = setPauseInstruction(testPool, authority, paused = false)
        assertEquals(0.toByte(), unpauseIx.data[8])
    }

    @Test
    fun `updateFeeInstruction has correct structure`() {
        val authority = PublicKey(ByteArray(32) { 12 })

        val ix = updateFeeInstruction(
            testPool, authority,
            UpdateFeeArgs(feeBps = BigInteger.valueOf(50))
        )

        assertEquals(2, ix.keys.size)
        assertEquals(16, ix.data.size)  // 8 + 8
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.UPDFEE))
    }

    @Test
    fun `createFarmInstruction has correct structure`() {
        val farm = PublicKey(ByteArray(32) { 20 })
        val rewardMint = PublicKey(ByteArray(32) { 21 })
        val authority = PublicKey(ByteArray(32) { 22 })

        val ix = createFarmInstruction(
            farm, testPool, rewardMint, authority,
            CreateFarmArgs(
                rewardRate = BigInteger.valueOf(1_000_000),
                startTime = 1704067200,
                endTime = 1704153600
            )
        )

        assertEquals(5, ix.keys.size)
        assertEquals(32, ix.data.size)  // 8 + 8 + 8 + 8
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.CREATEFARM))
    }

    @Test
    fun `stakeLpInstruction has correct structure`() {
        val userPosition = PublicKey(ByteArray(32) { 30 })
        val farm = PublicKey(ByteArray(32) { 31 })
        val lpVault = PublicKey(ByteArray(32) { 32 })

        val ix = stakeLpInstruction(
            userPosition, farm, testUserLp, lpVault, testUser,
            StakeArgs(amount = BigInteger.valueOf(1_000_000_000))
        )

        assertEquals(6, ix.keys.size)
        assertEquals(16, ix.data.size)  // 8 + 8
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.STAKELP))
    }

    @Test
    fun `getTwapInstruction has correct structure`() {
        val ix = getTwapInstruction(testPool, TwapWindow.HOUR_24)

        assertEquals(1, ix.keys.size)
        assertFalse(ix.keys[0].isWritable)
        assertEquals(9, ix.data.size)  // 8 + 1
        assertTrue(ix.data.sliceArray(0 until 8).contentEquals(Discriminators.GETTWAP))
        assertEquals(2.toByte(), ix.data[8])  // HOUR_24 = 2
    }

    @Test
    fun `all discriminators are 8 bytes`() {
        val allDiscriminators = listOf(
            Discriminators.CREATEPOOL, Discriminators.CREATEPN,
            Discriminators.INITT0V, Discriminators.INITT1V, Discriminators.INITLPM,
            Discriminators.SWAP, Discriminators.SWAPT0T1, Discriminators.SWAPT1T0,
            Discriminators.SWAPN, Discriminators.ADDLIQ, Discriminators.REMLIQ,
            Discriminators.SETPAUSE, Discriminators.UPDFEE, Discriminators.WDRAWFEE,
            Discriminators.COMMITAMP, Discriminators.RAMPAMP, Discriminators.STOPRAMP,
            Discriminators.INITAUTH, Discriminators.COMPLAUTH, Discriminators.CANCELAUTH,
            Discriminators.CREATEFARM, Discriminators.STAKELP, Discriminators.UNSTAKELP,
            Discriminators.CLAIMFARM, Discriminators.LOCKLP, Discriminators.CLAIMULP,
            Discriminators.GETTWAP
        )

        for (disc in allDiscriminators) {
            assertEquals(8, disc.size, "Discriminator should be 8 bytes")
        }
    }
}
