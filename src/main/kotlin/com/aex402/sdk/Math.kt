/**
 * AeX402 SDK - StableSwap Math
 *
 * Newton's method implementation for stable swap calculations.
 * All calculations use BigInteger for precision (equivalent to u128 in C).
 */
package com.aex402.sdk

import java.math.BigInteger

// ============================================================================
// Constants
// ============================================================================

private val ONE = BigInteger.ONE
private val TWO = BigInteger.valueOf(2)
private val THREE = BigInteger.valueOf(3)
private val FOUR = BigInteger.valueOf(4)
private val FEE_DENOMINATOR = BigInteger.valueOf(10000)
private val PRECISION = BigInteger.TEN.pow(18)

// ============================================================================
// Invariant Calculation
// ============================================================================

/**
 * Calculate invariant D for 2-token pool using Newton's method.
 *
 * The StableSwap invariant is:
 * A * n^n * sum(x_i) + D = A * n^n * D + D^(n+1) / (n^n * prod(x_i))
 *
 * For n=2:
 * A * 4 * (x + y) + D = A * 4 * D + D^3 / (4 * x * y)
 *
 * @param x Token 0 balance
 * @param y Token 1 balance
 * @param amp Amplification coefficient
 * @return Invariant D, or null if failed to converge
 */
fun calcD(x: BigInteger, y: BigInteger, amp: BigInteger): BigInteger? {
    val s = x + y
    if (s == BigInteger.ZERO) return BigInteger.ZERO

    var d = s
    val ann = amp * FOUR // A * n^n where n=2

    for (i in 0 until NEWTON_ITERATIONS) {
        // d_p = d^3 / (4 * x * y)
        var dP = d * d / (x * TWO)
        dP = dP * d / (y * TWO)

        val dPrev = d

        // d = (ann * s + d_p * 2) * d / ((ann - 1) * d + 3 * d_p)
        val num = (ann * s + dP * TWO) * d
        val denom = (ann - ONE) * d + dP * THREE
        d = num / denom

        // Check convergence
        if (d > dPrev) {
            if (d - dPrev <= ONE) return d
        } else {
            if (dPrev - d <= ONE) return d
        }
    }

    return null // Failed to converge
}

/**
 * Calculate invariant D for N-token pool using Newton's method.
 *
 * @param balances Token balances
 * @param amp Amplification coefficient
 * @return Invariant D, or null if failed to converge
 */
fun calcDN(balances: List<BigInteger>, amp: BigInteger): BigInteger? {
    val n = balances.size
    val nBig = BigInteger.valueOf(n.toLong())
    var s = BigInteger.ZERO
    for (b in balances) {
        s += b
    }
    if (s == BigInteger.ZERO) return BigInteger.ZERO

    var d = s
    val ann = amp * nBig.pow(n) // A * n^n

    for (i in 0 until NEWTON_ITERATIONS) {
        // d_p = d^(n+1) / (n^n * prod(x_i))
        var dP = d
        for (b in balances) {
            dP = dP * d / (b * nBig)
        }

        val dPrev = d

        // d = (ann * s + d_p * n) * d / ((ann - 1) * d + (n + 1) * d_p)
        val num = (ann * s + dP * nBig) * d
        val denom = (ann - ONE) * d + dP * BigInteger.valueOf((n + 1).toLong())
        d = num / denom

        if (d > dPrev) {
            if (d - dPrev <= ONE) return d
        } else {
            if (dPrev - d <= ONE) return d
        }
    }

    return null
}

// ============================================================================
// Output Amount Calculation
// ============================================================================

/**
 * Calculate output amount Y given new input balance X for 2-token swap.
 *
 * @param xNew New balance of input token after deposit
 * @param d Pool invariant
 * @param amp Amplification coefficient
 * @return Output token balance Y, or null if failed to converge
 */
fun calcY(xNew: BigInteger, d: BigInteger, amp: BigInteger): BigInteger? {
    val ann = amp * FOUR

    // c = d^3 / (4 * x_new * ann)
    var c = d * d / (xNew * TWO)
    c = c * d / (ann * TWO)

    // b = x_new + d / ann
    val b = xNew + d / ann

    var y = d

    for (i in 0 until NEWTON_ITERATIONS) {
        val yPrev = y

        // y = (y^2 + c) / (2y + b - d)
        val num = y * y + c
        val denom = y * TWO + b - d
        y = num / denom

        if (y > yPrev) {
            if (y - yPrev <= ONE) return y
        } else {
            if (yPrev - y <= ONE) return y
        }
    }

    return null
}

/**
 * Calculate output amount Y for N-token pool swap.
 *
 * @param xNew New balance of input token
 * @param balances Current balances (before swap)
 * @param outputIdx Index of output token
 * @param d Pool invariant
 * @param amp Amplification coefficient
 * @return New balance of output token, or null if failed to converge
 */
fun calcYN(
    xNew: BigInteger,
    balances: List<BigInteger>,
    inputIdx: Int,
    outputIdx: Int,
    d: BigInteger,
    amp: BigInteger
): BigInteger? {
    val n = balances.size
    val nBig = BigInteger.valueOf(n.toLong())
    val ann = amp * nBig.pow(n)

    // s' = sum of all balances except output
    // p = product of all balances except output
    var sExcluding = xNew
    var prod = xNew

    for (i in balances.indices) {
        if (i == outputIdx) continue
        if (i == inputIdx) continue
        sExcluding += balances[i]
        prod *= balances[i]
    }

    // c = D^(n+1) / (n^n * p * ann)
    var c = d
    for (j in 0 until n) {
        c = c * d / (nBig)
    }
    c = c / (prod * ann)

    // b = s' + D/ann
    val b = sExcluding + d / ann

    var y = d

    for (i in 0 until NEWTON_ITERATIONS) {
        val yPrev = y

        val num = y * y + c
        val denom = y * TWO + b - d
        if (denom <= BigInteger.ZERO) return null
        y = num / denom

        if (y > yPrev) {
            if (y - yPrev <= ONE) return y
        } else {
            if (yPrev - y <= ONE) return y
        }
    }

    return null
}

// ============================================================================
// Swap Simulation
// ============================================================================

/**
 * Simulate a swap and return output amount (after fees).
 *
 * @param balIn Input token balance
 * @param balOut Output token balance
 * @param amountIn Amount being swapped in
 * @param amp Amplification coefficient
 * @param feeBps Fee in basis points
 * @return Output amount after fee, or null if calculation failed
 */
fun simulateSwap(
    balIn: BigInteger,
    balOut: BigInteger,
    amountIn: BigInteger,
    amp: BigInteger,
    feeBps: BigInteger
): BigInteger? {
    val d = calcD(balIn, balOut, amp) ?: return null

    val newBalIn = balIn + amountIn
    val newBalOut = calcY(newBalIn, d, amp) ?: return null

    var amountOut = balOut - newBalOut
    if (amountOut < BigInteger.ZERO) return null

    // Apply fee
    val fee = amountOut * feeBps / FEE_DENOMINATOR
    amountOut -= fee

    return amountOut
}

/**
 * Simulate swap and return detailed result.
 */
data class SwapSimulationResult(
    val amountOut: BigInteger,
    val fee: BigInteger,
    val priceImpact: Double
)

fun simulateSwapDetailed(
    balIn: BigInteger,
    balOut: BigInteger,
    amountIn: BigInteger,
    amp: BigInteger,
    feeBps: BigInteger
): SwapSimulationResult? {
    val d = calcD(balIn, balOut, amp) ?: return null

    val newBalIn = balIn + amountIn
    val newBalOut = calcY(newBalIn, d, amp) ?: return null

    val amountOutBeforeFee = balOut - newBalOut
    if (amountOutBeforeFee < BigInteger.ZERO) return null

    val fee = amountOutBeforeFee * feeBps / FEE_DENOMINATOR
    val amountOut = amountOutBeforeFee - fee

    // Calculate price impact
    // Spot price = balOut / balIn
    // Effective price = amountOut / amountIn
    val spotPrice = balOut.toDouble() / balIn.toDouble()
    val effectivePrice = amountOut.toDouble() / amountIn.toDouble()
    val priceImpact = 1.0 - (effectivePrice / spotPrice)

    return SwapSimulationResult(amountOut, fee, priceImpact)
}

// ============================================================================
// Liquidity Calculations
// ============================================================================

/**
 * Calculate LP tokens for deposit (2-token pool).
 *
 * @param amt0 Amount of token 0 to deposit
 * @param amt1 Amount of token 1 to deposit
 * @param bal0 Current token 0 balance
 * @param bal1 Current token 1 balance
 * @param lpSupply Current LP token supply
 * @param amp Amplification coefficient
 * @return LP tokens to mint, or null if calculation failed
 */
fun calcLpTokens(
    amt0: BigInteger,
    amt1: BigInteger,
    bal0: BigInteger,
    bal1: BigInteger,
    lpSupply: BigInteger,
    amp: BigInteger
): BigInteger? {
    if (lpSupply == BigInteger.ZERO) {
        // Initial deposit: LP = sqrt(amt0 * amt1)
        return sqrt(amt0 * amt1)
    }

    val d0 = calcD(bal0, bal1, amp) ?: return null
    val d1 = calcD(bal0 + amt0, bal1 + amt1, amp) ?: return null

    if (d0 == BigInteger.ZERO) return null

    // LP tokens = lp_supply * (d1 - d0) / d0
    return lpSupply * (d1 - d0) / d0
}

/**
 * Calculate tokens received for LP burn.
 *
 * @param lpAmount LP tokens to burn
 * @param bal0 Current token 0 balance
 * @param bal1 Current token 1 balance
 * @param lpSupply Current LP token supply
 * @return Pair of (amount0, amount1), or null if calculation failed
 */
fun calcWithdraw(
    lpAmount: BigInteger,
    bal0: BigInteger,
    bal1: BigInteger,
    lpSupply: BigInteger
): Pair<BigInteger, BigInteger>? {
    if (lpSupply == BigInteger.ZERO) return null

    val amount0 = bal0 * lpAmount / lpSupply
    val amount1 = bal1 * lpAmount / lpSupply

    return Pair(amount0, amount1)
}

// ============================================================================
// Amp Ramping
// ============================================================================

/**
 * Calculate current amp during ramping.
 *
 * @param amp Initial amp
 * @param targetAmp Target amp
 * @param rampStart Ramp start timestamp
 * @param rampEnd Ramp end timestamp
 * @param now Current timestamp
 * @return Current effective amp
 */
fun getCurrentAmp(
    amp: BigInteger,
    targetAmp: BigInteger,
    rampStart: Long,
    rampEnd: Long,
    now: Long
): BigInteger {
    if (now >= rampEnd || rampEnd == rampStart) {
        return targetAmp
    }

    if (now <= rampStart) {
        return amp
    }

    val elapsed = BigInteger.valueOf(now - rampStart)
    val duration = BigInteger.valueOf(rampEnd - rampStart)

    return if (targetAmp > amp) {
        val diff = targetAmp - amp
        amp + diff * elapsed / duration
    } else {
        val diff = amp - targetAmp
        amp - diff * elapsed / duration
    }
}

// ============================================================================
// Virtual Price
// ============================================================================

/**
 * Calculate virtual price (LP value relative to underlying).
 *
 * @param bal0 Token 0 balance
 * @param bal1 Token 1 balance
 * @param lpSupply LP token supply
 * @param amp Amplification coefficient
 * @return Virtual price scaled by 1e18, or null if calculation failed
 */
fun calcVirtualPrice(
    bal0: BigInteger,
    bal1: BigInteger,
    lpSupply: BigInteger,
    amp: BigInteger
): BigInteger? {
    if (lpSupply == BigInteger.ZERO) return null

    val d = calcD(bal0, bal1, amp) ?: return null

    // Virtual price = D * 1e18 / lpSupply
    return d * PRECISION / lpSupply
}

// ============================================================================
// Price Impact
// ============================================================================

/**
 * Calculate price impact for a swap.
 *
 * @param balIn Input token balance
 * @param balOut Output token balance
 * @param amountIn Input amount
 * @param amp Amplification coefficient
 * @param feeBps Fee in basis points
 * @return Price impact as a fraction (0.01 = 1%), or null if calculation failed
 */
fun calcPriceImpact(
    balIn: BigInteger,
    balOut: BigInteger,
    amountIn: BigInteger,
    amp: BigInteger,
    feeBps: BigInteger
): Double? {
    val amountOut = simulateSwap(balIn, balOut, amountIn, amp, feeBps) ?: return null

    // Price impact = 1 - (amountOut / amountIn)
    // This is a simplified calculation assuming equal token value
    val ratio = amountOut.toDouble() / amountIn.toDouble()
    return 1.0 - ratio
}

// ============================================================================
// Slippage
// ============================================================================

/**
 * Calculate minimum output with slippage tolerance.
 *
 * @param expectedOutput Expected output amount
 * @param slippageBps Slippage tolerance in basis points
 * @return Minimum acceptable output
 */
fun calcMinOutput(expectedOutput: BigInteger, slippageBps: Int): BigInteger {
    val slippage = BigInteger.valueOf(slippageBps.toLong())
    return expectedOutput * (FEE_DENOMINATOR - slippage) / FEE_DENOMINATOR
}

// ============================================================================
// Integer Square Root
// ============================================================================

/**
 * Integer square root using Newton's method.
 */
fun sqrt(n: BigInteger): BigInteger {
    if (n == BigInteger.ZERO) return BigInteger.ZERO
    if (n <= THREE) return ONE

    var x = n
    var y = (x + ONE) / TWO

    while (y < x) {
        x = y
        y = (x + n / x) / TWO
    }

    return x
}

// ============================================================================
// Imbalance Check
// ============================================================================

/**
 * Check if swap would cause excessive imbalance.
 *
 * @param bal0 Token 0 balance
 * @param bal1 Token 1 balance
 * @param maxImbalanceRatio Maximum allowed imbalance ratio
 * @return true if within acceptable imbalance
 */
fun checkImbalance(
    bal0: BigInteger,
    bal1: BigInteger,
    maxImbalanceRatio: Double = 10.0
): Boolean {
    if (bal0 == BigInteger.ZERO || bal1 == BigInteger.ZERO) return false

    val ratio = if (bal0 > bal1) {
        bal0.toDouble() / bal1.toDouble()
    } else {
        bal1.toDouble() / bal0.toDouble()
    }

    return ratio <= maxImbalanceRatio
}

// ============================================================================
// Virtual Pool Math (Bonding Curve)
// ============================================================================

private val SCALE = BigInteger.valueOf(1_000_000_000L) // 1e9

/**
 * Calculate current price on bonding curve.
 *
 * price(t) = basePrice + slope * tokensSold / SCALE
 */
fun calcBondingPrice(
    basePrice: BigInteger,
    slope: BigInteger,
    tokensSold: BigInteger
): BigInteger {
    return basePrice + slope * tokensSold / SCALE
}

/**
 * Calculate tokens received for SOL input on bonding curve buy.
 *
 * Uses quadratic formula to solve:
 * solAmount = integral from tokensSold to (tokensSold + tokensOut) of price(t) dt
 */
fun calcBondingBuyTokens(
    solAmount: BigInteger,
    basePrice: BigInteger,
    slope: BigInteger,
    tokensSold: BigInteger
): BigInteger? {
    // price(t) = base + slope * t / SCALE
    // integral = base * delta + slope * (t1^2 - t0^2) / (2 * SCALE)
    //
    // Solving for delta given integral:
    // Let t0 = tokensSold, delta = tokensOut
    // solAmount = base * delta + slope * ((t0+delta)^2 - t0^2) / (2 * SCALE)
    //           = base * delta + slope * (2*t0*delta + delta^2) / (2 * SCALE)
    //           = delta * (base + slope * t0 / SCALE) + slope * delta^2 / (2 * SCALE)
    //
    // This is: a*delta^2 + b*delta - c = 0
    // where: a = slope / (2 * SCALE)
    //        b = base + slope * t0 / SCALE = currentPrice
    //        c = solAmount
    //
    // delta = (-b + sqrt(b^2 + 4*a*c)) / (2*a)
    //       = (-currentPrice + sqrt(currentPrice^2 + 2*slope*solAmount/SCALE)) * SCALE / slope

    val currentPrice = calcBondingPrice(basePrice, slope, tokensSold)

    if (slope == BigInteger.ZERO) {
        // Linear pricing: tokens = sol / price
        return solAmount * SCALE / currentPrice
    }

    // b^2 = currentPrice^2
    val b2 = currentPrice * currentPrice

    // 4*a*c = 4 * (slope / (2*SCALE)) * solAmount = 2 * slope * solAmount / SCALE
    val fourAC = TWO * slope * solAmount / SCALE

    val discriminant = b2 + fourAC
    val sqrtDisc = sqrt(discriminant)

    // delta = (sqrtDisc - currentPrice) * SCALE / slope
    val tokensOut = (sqrtDisc - currentPrice) * SCALE / slope

    return if (tokensOut >= BigInteger.ZERO) tokensOut else null
}

/**
 * Calculate SOL received for selling tokens on bonding curve.
 */
fun calcBondingSellSol(
    tokenAmount: BigInteger,
    basePrice: BigInteger,
    slope: BigInteger,
    tokensSold: BigInteger
): BigInteger? {
    // Can't sell more than what's been sold
    if (tokenAmount > tokensSold) return null

    val newTokensSold = tokensSold - tokenAmount

    // integral from newTokensSold to tokensSold of price(t) dt
    // = base * delta + slope * (t1^2 - t0^2) / (2 * SCALE)

    val t0 = newTokensSold
    val t1 = tokensSold
    val delta = t1 - t0

    val basePart = basePrice * delta
    val slopePart = slope * (t1 * t1 - t0 * t0) / (TWO * SCALE)

    val solOut = basePart + slopePart

    return if (solOut >= BigInteger.ZERO) solOut / SCALE else null
}
