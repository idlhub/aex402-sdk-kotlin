package com.aex402.sdk

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigInteger

class MathTest {

    @Test
    fun `calcD returns zero for zero balances`() {
        val d = calcD(BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(100))
        assertEquals(BigInteger.ZERO, d)
    }

    @Test
    fun `calcD converges for equal balances`() {
        val bal = BigInteger.valueOf(1_000_000_000_000L)
        val amp = BigInteger.valueOf(100)

        val d = calcD(bal, bal, amp)

        assertNotNull(d)
        // For equal balances, D should be approximately 2 * balance
        val expected = bal.multiply(BigInteger.TWO)
        val diff = d!!.subtract(expected).abs()
        assertTrue(diff < BigInteger.valueOf(1000), "D should be close to 2*balance")
    }

    @Test
    fun `calcD converges for imbalanced pool`() {
        val bal0 = BigInteger.valueOf(1_000_000_000_000L)
        val bal1 = BigInteger.valueOf(500_000_000_000L)
        val amp = BigInteger.valueOf(100)

        val d = calcD(bal0, bal1, amp)

        assertNotNull(d)
        assertTrue(d!! > BigInteger.ZERO)
    }

    @Test
    fun `calcY converges for valid inputs`() {
        val bal = BigInteger.valueOf(1_000_000_000_000L)
        val amp = BigInteger.valueOf(100)
        val d = calcD(bal, bal, amp)!!

        val xNew = bal.add(BigInteger.valueOf(1_000_000_000L))
        val y = calcY(xNew, d, amp)

        assertNotNull(y)
        assertTrue(y!! < bal, "Y should decrease when X increases")
    }

    @Test
    fun `simulateSwap returns correct output`() {
        val bal = BigInteger.valueOf(1_000_000_000_000L)
        val amountIn = BigInteger.valueOf(1_000_000_000L)
        val amp = BigInteger.valueOf(100)
        val feeBps = BigInteger.valueOf(30)

        val amountOut = simulateSwap(bal, bal, amountIn, amp, feeBps)

        assertNotNull(amountOut)
        assertTrue(amountOut!! > BigInteger.ZERO)
        assertTrue(amountOut < amountIn, "Output should be less than input (stable swap)")
    }

    @Test
    fun `simulateSwap deducts fee`() {
        val bal = BigInteger.valueOf(1_000_000_000_000L)
        val amountIn = BigInteger.valueOf(1_000_000_000L)
        val amp = BigInteger.valueOf(100)

        val withFee = simulateSwap(bal, bal, amountIn, amp, BigInteger.valueOf(30))
        val noFee = simulateSwap(bal, bal, amountIn, amp, BigInteger.ZERO)

        assertNotNull(withFee)
        assertNotNull(noFee)
        assertTrue(noFee!! > withFee!!, "Output with fee should be less than without fee")
    }

    @Test
    fun `calcLpTokens returns sqrt for initial deposit`() {
        val amt = BigInteger.valueOf(1_000_000_000L)
        val amp = BigInteger.valueOf(100)

        val lpTokens = calcLpTokens(
            amt, amt,
            BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO,
            amp
        )

        assertNotNull(lpTokens)
        // Initial LP = sqrt(amt0 * amt1) = amt for equal amounts
        assertEquals(amt, lpTokens)
    }

    @Test
    fun `calcLpTokens returns proportional for existing pool`() {
        val bal = BigInteger.valueOf(1_000_000_000_000L)
        val lpSupply = BigInteger.valueOf(1_000_000_000_000L)
        val amt = BigInteger.valueOf(100_000_000_000L) // 10% deposit
        val amp = BigInteger.valueOf(100)

        val lpTokens = calcLpTokens(amt, amt, bal, bal, lpSupply, amp)

        assertNotNull(lpTokens)
        // Should get ~10% of LP supply for 10% deposit
        val expectedMin = lpSupply.divide(BigInteger.valueOf(11))
        val expectedMax = lpSupply.divide(BigInteger.valueOf(9))
        assertTrue(lpTokens!! > expectedMin && lpTokens < expectedMax)
    }

    @Test
    fun `calcWithdraw returns proportional amounts`() {
        val bal0 = BigInteger.valueOf(1_000_000_000_000L)
        val bal1 = BigInteger.valueOf(2_000_000_000_000L)
        val lpSupply = BigInteger.valueOf(1_000_000_000_000L)
        val lpAmount = BigInteger.valueOf(100_000_000_000L) // 10%

        val result = calcWithdraw(lpAmount, bal0, bal1, lpSupply)

        assertNotNull(result)
        val (amount0, amount1) = result!!
        assertEquals(BigInteger.valueOf(100_000_000_000L), amount0)
        assertEquals(BigInteger.valueOf(200_000_000_000L), amount1)
    }

    @Test
    fun `getCurrentAmp returns target after ramp`() {
        val amp = BigInteger.valueOf(100)
        val targetAmp = BigInteger.valueOf(200)
        val rampStart = 1000L
        val rampEnd = 2000L
        val now = 3000L

        val current = getCurrentAmp(amp, targetAmp, rampStart, rampEnd, now)

        assertEquals(targetAmp, current)
    }

    @Test
    fun `getCurrentAmp returns initial before ramp`() {
        val amp = BigInteger.valueOf(100)
        val targetAmp = BigInteger.valueOf(200)
        val rampStart = 1000L
        val rampEnd = 2000L
        val now = 500L

        val current = getCurrentAmp(amp, targetAmp, rampStart, rampEnd, now)

        assertEquals(amp, current)
    }

    @Test
    fun `getCurrentAmp interpolates during ramp`() {
        val amp = BigInteger.valueOf(100)
        val targetAmp = BigInteger.valueOf(200)
        val rampStart = 1000L
        val rampEnd = 2000L
        val now = 1500L // 50% through

        val current = getCurrentAmp(amp, targetAmp, rampStart, rampEnd, now)

        // Should be 150 (halfway between 100 and 200)
        assertEquals(BigInteger.valueOf(150), current)
    }

    @Test
    fun `sqrt returns correct results`() {
        assertEquals(BigInteger.ZERO, sqrt(BigInteger.ZERO))
        assertEquals(BigInteger.ONE, sqrt(BigInteger.ONE))
        assertEquals(BigInteger.ONE, sqrt(BigInteger.valueOf(3)))
        assertEquals(BigInteger.valueOf(10), sqrt(BigInteger.valueOf(100)))
        assertEquals(BigInteger.valueOf(1000), sqrt(BigInteger.valueOf(1_000_000)))
        assertEquals(BigInteger.valueOf(1000000), sqrt(BigInteger.valueOf(1_000_000_000_000)))
    }

    @Test
    fun `calcMinOutput applies slippage correctly`() {
        val expected = BigInteger.valueOf(1_000_000_000L)
        val slippageBps = 100 // 1%

        val minOutput = calcMinOutput(expected, slippageBps)

        // Should be 99% of expected
        val expectedMin = BigInteger.valueOf(990_000_000L)
        assertEquals(expectedMin, minOutput)
    }

    @Test
    fun `calcVirtualPrice returns correct value`() {
        val bal = BigInteger.valueOf(1_000_000_000_000L)
        val lpSupply = BigInteger.valueOf(2_000_000_000_000L)
        val amp = BigInteger.valueOf(100)

        val virtualPrice = calcVirtualPrice(bal, bal, lpSupply, amp)

        assertNotNull(virtualPrice)
        // Virtual price = D * 1e18 / lpSupply
        // D ~ 2*bal, so VP ~ 1e18 for balanced pool with lpSupply = 2*bal
        assertTrue(virtualPrice!! > BigInteger.ZERO)
    }

    @Test
    fun `checkImbalance returns true for balanced pool`() {
        val bal0 = BigInteger.valueOf(1_000_000_000_000L)
        val bal1 = BigInteger.valueOf(1_000_000_000_000L)

        assertTrue(checkImbalance(bal0, bal1))
    }

    @Test
    fun `checkImbalance returns false for zero balance`() {
        val bal0 = BigInteger.valueOf(1_000_000_000_000L)
        val bal1 = BigInteger.ZERO

        assertFalse(checkImbalance(bal0, bal1))
    }

    @Test
    fun `checkImbalance returns false for extreme imbalance`() {
        val bal0 = BigInteger.valueOf(1_000_000_000_000L)
        val bal1 = BigInteger.valueOf(10_000_000_000L) // 100:1 ratio

        assertFalse(checkImbalance(bal0, bal1, 10.0))
    }
}
