/**
 * AeX402 SDK - High-Level Client
 *
 * Async client for interacting with the AeX402 AMM.
 * Uses coroutines for async operations and OkHttp for RPC calls.
 */
package com.aex402.sdk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigInteger
import java.util.Base64
import java.util.concurrent.TimeUnit

// ============================================================================
// RPC Client
// ============================================================================

/**
 * Solana JSON-RPC client.
 */
class SolanaRpc(
    private val endpoint: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    /**
     * Get account info.
     */
    suspend fun getAccountInfo(pubkey: PublicKey): ByteArray? = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getAccountInfo")
            put("params", buildJsonArray {
                add(pubkey.toBase58())
                add(buildJsonObject {
                    put("encoding", "base64")
                })
            })
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext null

        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val result = jsonResponse["result"]?.jsonObject?.get("value") ?: return@withContext null

        if (result is JsonNull) return@withContext null

        val data = result.jsonObject["data"]?.jsonArray?.get(0)?.jsonPrimitive?.content
            ?: return@withContext null

        Base64.getDecoder().decode(data)
    }

    /**
     * Get multiple accounts.
     */
    suspend fun getMultipleAccounts(pubkeys: List<PublicKey>): List<ByteArray?> = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getMultipleAccounts")
            put("params", buildJsonArray {
                add(buildJsonArray {
                    pubkeys.forEach { add(it.toBase58()) }
                })
                add(buildJsonObject {
                    put("encoding", "base64")
                })
            })
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext pubkeys.map { null }

        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val result = jsonResponse["result"]?.jsonObject?.get("value")?.jsonArray
            ?: return@withContext pubkeys.map { null }

        result.map { item ->
            if (item is JsonNull) return@map null
            val data = item.jsonObject["data"]?.jsonArray?.get(0)?.jsonPrimitive?.content
                ?: return@map null
            Base64.getDecoder().decode(data)
        }
    }

    /**
     * Get current slot.
     */
    suspend fun getSlot(): Long = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getSlot")
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext 0L

        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        jsonResponse["result"]?.jsonPrimitive?.long ?: 0L
    }

    /**
     * Get block time.
     */
    suspend fun getBlockTime(slot: Long): Long? = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getBlockTime")
            put("params", buildJsonArray { add(slot) })
        }.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext null

        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        jsonResponse["result"]?.jsonPrimitive?.longOrNull
    }
}

// ============================================================================
// AeX402 Client
// ============================================================================

/**
 * High-level client for interacting with the AeX402 AMM.
 */
class AeX402Client(
    private val rpc: SolanaRpc,
    val programId: PublicKey = PublicKey(PROGRAM_ID)
) {
    // ========== Account Fetching ==========

    /**
     * Fetch and parse a Pool account.
     */
    suspend fun getPool(address: PublicKey): Pool? {
        val data = rpc.getAccountInfo(address) ?: return null
        return parsePool(data)
    }

    /**
     * Fetch and parse an NPool account.
     */
    suspend fun getNPool(address: PublicKey): NPool? {
        val data = rpc.getAccountInfo(address) ?: return null
        return parseNPool(data)
    }

    /**
     * Fetch and parse a Farm account.
     */
    suspend fun getFarm(address: PublicKey): Farm? {
        val data = rpc.getAccountInfo(address) ?: return null
        return parseFarm(data)
    }

    /**
     * Fetch and parse a UserFarm account.
     */
    suspend fun getUserFarm(address: PublicKey): UserFarm? {
        val data = rpc.getAccountInfo(address) ?: return null
        return parseUserFarm(data)
    }

    /**
     * Fetch and parse a Lottery account.
     */
    suspend fun getLottery(address: PublicKey): Lottery? {
        val data = rpc.getAccountInfo(address) ?: return null
        return parseLottery(data)
    }

    // ========== Swap Simulation ==========

    /**
     * Simulate a swap and return expected output.
     */
    suspend fun simulateSwap(
        poolAddress: PublicKey,
        amountIn: BigInteger,
        direction: SwapDirection
    ): BigInteger? {
        val pool = getPool(poolAddress) ?: return null

        val (balIn, balOut) = when (direction) {
            SwapDirection.TOKEN0_TO_TOKEN1 -> Pair(pool.bal0, pool.bal1)
            SwapDirection.TOKEN1_TO_TOKEN0 -> Pair(pool.bal1, pool.bal0)
        }

        val now = System.currentTimeMillis() / 1000
        val currentAmp = getCurrentAmp(
            pool.amp,
            pool.targetAmp,
            pool.rampStart,
            pool.rampStop,
            now
        )

        return simulateSwap(balIn, balOut, amountIn, currentAmp, pool.feeBps)
    }

    /**
     * Simulate a swap with detailed result.
     */
    suspend fun simulateSwapDetailed(
        poolAddress: PublicKey,
        amountIn: BigInteger,
        direction: SwapDirection
    ): SwapSimulationResult? {
        val pool = getPool(poolAddress) ?: return null

        val (balIn, balOut) = when (direction) {
            SwapDirection.TOKEN0_TO_TOKEN1 -> Pair(pool.bal0, pool.bal1)
            SwapDirection.TOKEN1_TO_TOKEN0 -> Pair(pool.bal1, pool.bal0)
        }

        val now = System.currentTimeMillis() / 1000
        val currentAmp = getCurrentAmp(
            pool.amp,
            pool.targetAmp,
            pool.rampStart,
            pool.rampStop,
            now
        )

        return simulateSwapDetailed(balIn, balOut, amountIn, currentAmp, pool.feeBps)
    }

    // ========== Liquidity Simulation ==========

    /**
     * Calculate expected LP tokens for deposit.
     */
    suspend fun simulateAddLiquidity(
        poolAddress: PublicKey,
        amount0: BigInteger,
        amount1: BigInteger
    ): BigInteger? {
        val pool = getPool(poolAddress) ?: return null

        val now = System.currentTimeMillis() / 1000
        val currentAmp = getCurrentAmp(
            pool.amp,
            pool.targetAmp,
            pool.rampStart,
            pool.rampStop,
            now
        )

        return calcLpTokens(
            amount0, amount1,
            pool.bal0, pool.bal1,
            pool.lpSupply,
            currentAmp
        )
    }

    /**
     * Calculate tokens received for LP burn.
     */
    suspend fun simulateRemoveLiquidity(
        poolAddress: PublicKey,
        lpAmount: BigInteger
    ): Pair<BigInteger, BigInteger>? {
        val pool = getPool(poolAddress) ?: return null
        return calcWithdraw(lpAmount, pool.bal0, pool.bal1, pool.lpSupply)
    }

    // ========== Analytics ==========

    /**
     * Get pool statistics.
     */
    suspend fun getPoolStats(poolAddress: PublicKey): PoolStats? {
        val pool = getPool(poolAddress) ?: return null

        val now = System.currentTimeMillis() / 1000
        val currentAmp = getCurrentAmp(
            pool.amp,
            pool.targetAmp,
            pool.rampStart,
            pool.rampStop,
            now
        )

        val virtualPrice = calcVirtualPrice(
            pool.bal0,
            pool.bal1,
            pool.lpSupply,
            currentAmp
        )

        return PoolStats(
            tvl0 = pool.bal0,
            tvl1 = pool.bal1,
            volume24h = pool.vol0 + pool.vol1,
            swapCount = pool.tradeCount.toLong(),
            feeBps = pool.feeBps,
            amp = currentAmp,
            virtualPrice = virtualPrice,
            paused = pool.paused
        )
    }

    /**
     * Get decoded OHLCV candles.
     */
    suspend fun getCandles(
        poolAddress: PublicKey,
        type: CandleType
    ): List<CandleDecoded>? {
        val pool = getPool(poolAddress) ?: return null

        val candles = when (type) {
            CandleType.HOURLY -> pool.hourlyCandles
            CandleType.DAILY -> pool.dailyCandles
        }

        return candles.map { it.decode() }
    }

    // ========== Utilities ==========

    /**
     * Calculate price impact for a swap.
     */
    suspend fun getPriceImpact(
        poolAddress: PublicKey,
        amountIn: BigInteger,
        direction: SwapDirection
    ): Double? {
        val pool = getPool(poolAddress) ?: return null

        val (balIn, balOut) = when (direction) {
            SwapDirection.TOKEN0_TO_TOKEN1 -> Pair(pool.bal0, pool.bal1)
            SwapDirection.TOKEN1_TO_TOKEN0 -> Pair(pool.bal1, pool.bal0)
        }

        val now = System.currentTimeMillis() / 1000
        val currentAmp = getCurrentAmp(
            pool.amp,
            pool.targetAmp,
            pool.rampStart,
            pool.rampStop,
            now
        )

        return calcPriceImpact(balIn, balOut, amountIn, currentAmp, pool.feeBps)
    }

    /**
     * Get minimum output with slippage tolerance.
     */
    fun getMinOutput(expectedOutput: BigInteger, slippageBps: Int): BigInteger {
        return calcMinOutput(expectedOutput, slippageBps)
    }

    /**
     * Derive pool PDA.
     */
    fun derivePool(mint0: PublicKey, mint1: PublicKey): Pair<PublicKey, Int> {
        return derivePoolPda(mint0, mint1, programId)
    }

    /**
     * Derive farm PDA.
     */
    fun deriveFarm(pool: PublicKey): Pair<PublicKey, Int> {
        return deriveFarmPda(pool, programId)
    }

    /**
     * Derive user farm PDA.
     */
    fun deriveUserFarm(farm: PublicKey, user: PublicKey): Pair<PublicKey, Int> {
        return deriveUserFarmPda(farm, user, programId)
    }
}

// ============================================================================
// Enums and Data Classes
// ============================================================================

/**
 * Swap direction enum.
 */
enum class SwapDirection {
    TOKEN0_TO_TOKEN1,
    TOKEN1_TO_TOKEN0
}

/**
 * Candle type enum.
 */
enum class CandleType {
    HOURLY,
    DAILY
}

/**
 * Pool statistics.
 */
data class PoolStats(
    val tvl0: BigInteger,
    val tvl1: BigInteger,
    val volume24h: BigInteger,
    val swapCount: Long,
    val feeBps: BigInteger,
    val amp: BigInteger,
    val virtualPrice: BigInteger?,
    val paused: Boolean
)

// ============================================================================
// Factory Functions
// ============================================================================

/**
 * Create an AeX402 client with the given RPC endpoint.
 */
fun createClient(
    rpcEndpoint: String,
    programId: PublicKey = PublicKey(PROGRAM_ID)
): AeX402Client {
    val rpc = SolanaRpc(rpcEndpoint)
    return AeX402Client(rpc, programId)
}

/**
 * Create an AeX402 client for Solana Devnet.
 */
fun createDevnetClient(): AeX402Client {
    return createClient("https://api.devnet.solana.com")
}

/**
 * Create an AeX402 client for Solana Mainnet.
 */
fun createMainnetClient(): AeX402Client {
    return createClient("https://api.mainnet-beta.solana.com")
}

// ============================================================================
// Backwards Compatibility
// ============================================================================

/**
 * Alias for backwards compatibility.
 */
typealias StableSwapClient = AeX402Client
