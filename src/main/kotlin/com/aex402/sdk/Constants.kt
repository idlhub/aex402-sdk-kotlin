/**
 * AeX402 SDK - Constants
 *
 * Program ID, discriminators, and configuration constants for the AeX402 Hybrid AMM
 */
package com.aex402.sdk

import java.math.BigInteger

// ============================================================================
// Program ID
// ============================================================================

/** AeX402 Program ID on Solana Devnet/Mainnet */
const val PROGRAM_ID = "3AMM53MsJZy2Jvf7PeHHga3bsGjWV4TSaYz29WUtcdje"

// ============================================================================
// Token Programs
// ============================================================================

/** SPL Token Program ID */
const val TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"

/** Token-2022 Program ID */
const val TOKEN_2022_PROGRAM_ID = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"

/** System Program ID */
const val SYSTEM_PROGRAM_ID = "11111111111111111111111111111111"

// ============================================================================
// Pool Constants
// ============================================================================

val MIN_AMP = BigInteger.ONE
val MAX_AMP = BigInteger.valueOf(100000)
val DEFAULT_FEE_BPS = BigInteger.valueOf(30)
val ADMIN_FEE_PCT = BigInteger.valueOf(50)
val MIN_SWAP = BigInteger.valueOf(100000)
val MIN_DEPOSIT = BigInteger.valueOf(100000000)
const val NEWTON_ITERATIONS = 255
const val RAMP_MIN_DURATION = 86400L // 1 day in seconds
const val COMMIT_DELAY = 3600L // 1 hour in seconds
val MIGRATION_FEE_BPS = BigInteger.valueOf(1337)
const val MAX_TOKENS = 8

// ============================================================================
// Account Sizes
// ============================================================================

const val POOL_SIZE = 1024
const val NPOOL_SIZE = 2048
const val FARM_SIZE = 120
const val USER_FARM_SIZE = 96
const val LOTTERY_SIZE = 152
const val LOTTERY_ENTRY_SIZE = 88

// ============================================================================
// Analytics Constants
// ============================================================================

const val BLOOM_SIZE = 128
const val OHLCV_24H = 24
const val OHLCV_7D = 7
const val SLOTS_PER_HOUR = 9000    // ~400ms * 9000 = 1 hour
const val SLOTS_PER_DAY = 216000   // 24 hours

// ============================================================================
// Instruction Discriminators (8-byte little-endian)
// ============================================================================

/**
 * Instruction discriminators for all AeX402 handlers.
 * These are 8-byte values stored as ByteArray in little-endian format.
 */
object Discriminators {
    // Pool creation
    val CREATEPOOL = byteArrayOf(0xf9.toByte(), 0xe3.toByte(), 0xa7.toByte(), 0xc8.toByte(), 0xd1.toByte(), 0xe4.toByte(), 0xb9.toByte(), 0xf2.toByte())
    val CREATEPN = byteArrayOf(0x1b, 0x7c, 0xc5.toByte(), 0xe5.toByte(), 0xbc.toByte(), 0x33, 0x9c.toByte(), 0x27)
    val INITT0V = byteArrayOf(0x9f.toByte(), 0x4a, 0x3e, 0x0f, 0x0d, 0x3b, 0x8c.toByte(), 0x5e)
    val INITT1V = byteArrayOf(0x8a.toByte(), 0x5e, 0x2d, 0x3b, 0x1c, 0x9f.toByte(), 0x4e, 0x7a)
    val INITLPM = byteArrayOf(0xf2.toByte(), 0xe7.toByte(), 0xb8.toByte(), 0xc5.toByte(), 0xa3.toByte(), 0xe9.toByte(), 0xd1.toByte(), 0xf4.toByte())

    // Swaps
    val SWAP = byteArrayOf(0xc8.toByte(), 0x87.toByte(), 0x75, 0xe1.toByte(), 0x91.toByte(), 0x9e.toByte(), 0xc6.toByte(), 0x82.toByte())
    val SWAPT0T1 = byteArrayOf(0x2a, 0x4e, 0xf1.toByte(), 0xe0.toByte(), 0xb7.toByte(), 0xf2.toByte(), 0x2a, 0x64)
    val SWAPT1T0 = byteArrayOf(0xc8.toByte(), 0xc4.toByte(), 0x75, 0xac.toByte(), 0x1b, 0x13, 0x0e, 0x3a)
    val SWAPN = byteArrayOf(0xf8.toByte(), 0xe5.toByte(), 0xd9.toByte(), 0xb2.toByte(), 0xc7.toByte(), 0xe3.toByte(), 0xa8.toByte(), 0xf1.toByte())
    val MIGT0T1 = byteArrayOf(0xd5.toByte(), 0xe9.toByte(), 0xb7.toByte(), 0xc3.toByte(), 0xa8.toByte(), 0xf1.toByte(), 0xe4.toByte(), 0xd2.toByte())
    val MIGT1T0 = byteArrayOf(0xb8.toByte(), 0x3d, 0x39, 0x26, 0x94.toByte(), 0x77, 0x88.toByte(), 0x18)

    // Liquidity
    val ADDLIQ = byteArrayOf(0xa9.toByte(), 0xe5.toByte(), 0xd1.toByte(), 0xb3.toByte(), 0xf8.toByte(), 0xc4.toByte(), 0xe7.toByte(), 0xa2.toByte())
    val ADDLIQ1 = byteArrayOf(0xe6.toByte(), 0x12, 0x2e, 0x3c, 0x4e, 0x8b.toByte(), 0xc9.toByte(), 0x51)
    val ADDLIQN = byteArrayOf(0xf6.toByte(), 0xe4.toByte(), 0xe9.toByte(), 0xb1.toByte(), 0xa8.toByte(), 0xc2.toByte(), 0xf7.toByte(), 0xe3.toByte())
    val REMLIQ = byteArrayOf(0x02, 0xf9.toByte(), 0xc5.toByte(), 0x75, 0x2c, 0xbc.toByte(), 0x54, 0x2e)
    val REMLIQN = byteArrayOf(0xb4.toByte(), 0xb1.toByte(), 0xe9.toByte(), 0xd7.toByte(), 0xc5.toByte(), 0xa2.toByte(), 0xe8.toByte(), 0xb3.toByte())

    // Admin
    val SETPAUSE = byteArrayOf(0xc9.toByte(), 0x6e, 0x0d, 0x7e, 0x2b, 0x76, 0x75, 0xe0.toByte())
    val UPDFEE = byteArrayOf(0x4a, 0x1f, 0x9d.toByte(), 0x7c, 0x5b, 0x2e, 0x3a, 0x8f.toByte())
    val WDRAWFEE = byteArrayOf(0xf8.toByte(), 0xe7.toByte(), 0xb1.toByte(), 0xc8.toByte(), 0xa2.toByte(), 0xd3.toByte(), 0xe5.toByte(), 0xf9.toByte())
    val COMMITAMP = byteArrayOf(0xc4.toByte(), 0xe2.toByte(), 0xb8.toByte(), 0xa5.toByte(), 0xf7.toByte(), 0xe3.toByte(), 0xd9.toByte(), 0xc1.toByte())
    val RAMPAMP = byteArrayOf(0x6a, 0x8e.toByte(), 0x2d, 0x7b, 0x3f, 0x5e, 0x1c, 0x9a.toByte())
    val STOPRAMP = byteArrayOf(0x53, 0x10, 0xa2.toByte(), 0x15, 0xbb.toByte(), 0x27, 0x94.toByte(), 0x3c)
    val INITAUTH = byteArrayOf(0xf4.toByte(), 0xf8.toByte(), 0xe1.toByte(), 0xb3.toByte(), 0xc9.toByte(), 0xa7.toByte(), 0xe2.toByte(), 0xf5.toByte())
    val COMPLAUTH = byteArrayOf(0xf5.toByte(), 0xe1.toByte(), 0xe9.toByte(), 0xb7.toByte(), 0xa4.toByte(), 0xd2.toByte(), 0xe8.toByte(), 0xf6.toByte())
    val CANCELAUTH = byteArrayOf(0xf6.toByte(), 0xe8.toByte(), 0xb2.toByte(), 0xd5.toByte(), 0xc1.toByte(), 0xa9.toByte(), 0xe3.toByte(), 0xf7.toByte())

    // Farming
    val CREATEFARM = byteArrayOf(0x5c, 0x5d, 0x1a, 0x2f, 0x8e.toByte(), 0x0c, 0x7b, 0x6d)
    val STAKELP = byteArrayOf(0xf7.toByte(), 0xe2.toByte(), 0xb9.toByte(), 0xb3.toByte(), 0xa7.toByte(), 0xe1.toByte(), 0xd4.toByte(), 0xf8.toByte())
    val UNSTAKELP = byteArrayOf(0xbc.toByte(), 0xf8.toByte(), 0x34, 0x4e, 0x65, 0xbf.toByte(), 0x66, 0x41)
    val CLAIMFARM = byteArrayOf(0x9b.toByte(), 0xec.toByte(), 0xd6.toByte(), 0xe0.toByte(), 0xb7.toByte(), 0x62, 0x75, 0x07)
    val LOCKLP = byteArrayOf(0xec.toByte(), 0x8c.toByte(), 0x02, 0x5f, 0x01, 0x83.toByte(), 0xfb.toByte(), 0xfe.toByte())
    val CLAIMULP = byteArrayOf(0x1e, 0x8b.toByte(), 0xe8.toByte(), 0x5c, 0xf4.toByte(), 0x93.toByte(), 0x85.toByte(), 0xca.toByte())

    // Lottery
    val CREATELOT = byteArrayOf(0x3c, 0x79, 0x72, 0x65, 0x74, 0x74, 0x6f, 0x6c)
    val ENTERLOT = byteArrayOf(0xfc.toByte(), 0x48, 0xef.toByte(), 0x4e, 0x3a, 0x38, 0x95.toByte(), 0xe7.toByte())
    val DRAWLOT = byteArrayOf(0x11, 0xbc.toByte(), 0x7c, 0x4d, 0x5a, 0x22, 0x61, 0x13)
    val CLAIMLOT = byteArrayOf(0xf4.toByte(), 0x3c, 0x9f.toByte(), 0x15, 0x3f, 0x5e, 0x7b, 0x7e)

    // Registry
    val INITREG = byteArrayOf(0x18, 0x07, 0x60, 0xf5.toByte(), 0xd4.toByte(), 0xc3.toByte(), 0xb2.toByte(), 0xa1.toByte())
    val REGPOOL = byteArrayOf(0x29, 0x18, 0x07, 0xf6.toByte(), 0xe5.toByte(), 0xd4.toByte(), 0xc3.toByte(), 0xb2.toByte())
    val UNREGPOOL = byteArrayOf(0x30, 0x29, 0x18, 0x07, 0xf6.toByte(), 0xe5.toByte(), 0xd4.toByte(), 0xc3.toByte())

    // Oracle
    val GETTWAP = byteArrayOf(0x01, 0x74, 0x65, 0x67, 0x61, 0x70, 0x77, 0x74)

    // Circuit Breaker
    val SETCB = byteArrayOf(0x01, 0xcb.toByte(), 0x01, 0xcb.toByte(), 0x01, 0xcb.toByte(), 0x01, 0xcb.toByte())
    val RESETCB = byteArrayOf(0x02, 0xcb.toByte(), 0x02, 0xcb.toByte(), 0x02, 0xcb.toByte(), 0x02, 0xcb.toByte())

    // Rate Limiting
    val SETRL = byteArrayOf(0x6c, 0x72, 0x01, 0x6c, 0x72, 0x01, 0x6c, 0x72)

    // Oracle Config
    val SETORACLE = byteArrayOf(0x04, 0x03, 0x02, 0x01, 0x6c, 0x63, 0x72, 0x6f)

    // Governance
    val GOVPROP = byteArrayOf(0x00, 0x70, 0x6f, 0x72, 0x70, 0x76, 0x6f, 0x67)
    val GOVVOTE = byteArrayOf(0x00, 0x65, 0x74, 0x6f, 0x76, 0x76, 0x6f, 0x67)
    val GOVEXEC = byteArrayOf(0x63, 0x65, 0x78, 0x65, 0x76, 0x6f, 0x67, 0x00)
    val GOVCNCL = byteArrayOf(0x6c, 0x63, 0x6e, 0x63, 0x76, 0x6f, 0x67, 0x00)

    // Orderbook
    val INITBOOK = byteArrayOf(0x6b, 0x6f, 0x6f, 0x62, 0x74, 0x69, 0x6e, 0x69)
    val PLACEORD = byteArrayOf(0x64, 0x72, 0x6f, 0x65, 0x63, 0x61, 0x6c, 0x70)
    val CANCELORD = byteArrayOf(0x72, 0x6f, 0x6c, 0x65, 0x63, 0x6e, 0x61, 0x63)
    val FILLORD = byteArrayOf(0x65, 0x64, 0x72, 0x6f, 0x6c, 0x6c, 0x69, 0x66)

    // Concentrated Liquidity
    val INITCLPL = byteArrayOf(0x01, 0x01, 0x6c, 0x6f, 0x6f, 0x70, 0x6c, 0x63)
    val CLMINT = byteArrayOf(0x01, 0x01, 0x74, 0x6e, 0x69, 0x6d, 0x6c, 0x63)
    val CLBURN = byteArrayOf(0x01, 0x01, 0x6e, 0x72, 0x75, 0x62, 0x6c, 0x63)
    val CLCOLLECT = byteArrayOf(0x63, 0x65, 0x6c, 0x6c, 0x6f, 0x63, 0x6c, 0x63)
    val CLSWAP = byteArrayOf(0x01, 0x01, 0x70, 0x61, 0x77, 0x73, 0x6c, 0x63)

    // Flash Loans
    val FLASHLOAN = byteArrayOf(0x61, 0x6f, 0x6c, 0x68, 0x73, 0x61, 0x6c, 0x66)
    val FLASHREPY = byteArrayOf(0x70, 0x65, 0x72, 0x68, 0x73, 0x61, 0x6c, 0x66)

    // Multi-hop
    val MULTIHOP = byteArrayOf(0x70, 0x6f, 0x68, 0x69, 0x74, 0x6c, 0x75, 0x6d)

    // ML Brain
    val INITML = byteArrayOf(0x72, 0x62, 0x6c, 0x6d, 0x74, 0x69, 0x6e, 0x69)
    val CFGML = byteArrayOf(0x61, 0x72, 0x62, 0x6c, 0x6d, 0x67, 0x66, 0x63)
    val TRAINML = byteArrayOf(0x00, 0x6c, 0x6d, 0x6e, 0x69, 0x61, 0x72, 0x74)
    val APPLYML = byteArrayOf(0x00, 0x6c, 0x6d, 0x79, 0x6c, 0x70, 0x70, 0x61)
    val LOGML = byteArrayOf(0x61, 0x74, 0x73, 0x6c, 0x6d, 0x67, 0x6f, 0x6c)

    // Transfer Hook
    val TH_EXEC = byteArrayOf(0x69, 0x25, 0x65, 0xc5.toByte(), 0x4b, 0xfb.toByte(), 0x66, 0x1a)
    val TH_INIT = byteArrayOf(0x2b, 0x22, 0x0d, 0x31, 0xa7.toByte(), 0x58, 0xeb.toByte(), 0xeb.toByte())

    // Virtual Pools
    val INITGLOBAL = byteArrayOf(0x62, 0x6f, 0x6c, 0x67, 0x74, 0x69, 0x6e, 0x69)
    val VPCREATE = byteArrayOf(0x65, 0x74, 0x61, 0x65, 0x72, 0x63, 0x70, 0x76)
    val VPBUY = byteArrayOf(0x00, 0x00, 0x00, 0x79, 0x75, 0x62, 0x70, 0x76)
    val VPSELL = byteArrayOf(0x00, 0x00, 0x6c, 0x6c, 0x65, 0x73, 0x70, 0x76)
    val VPGRAD = byteArrayOf(0x00, 0x00, 0x64, 0x61, 0x72, 0x67, 0x70, 0x76)
    val VPCLAIM = byteArrayOf(0x00, 0x6d, 0x69, 0x61, 0x6c, 0x63, 0x70, 0x76)
    val VPFLUSH = byteArrayOf(0x00, 0x68, 0x73, 0x75, 0x6c, 0x66, 0x70, 0x76)
    val CLAIMCRT = byteArrayOf(0x74, 0x72, 0x63, 0x6d, 0x69, 0x61, 0x6c, 0x63)
}

// ============================================================================
// Account Discriminators (8-byte ASCII strings)
// ============================================================================

/**
 * Account discriminators for verifying account types.
 * These are 8-byte ASCII strings used as magic headers.
 */
object AccountDiscriminators {
    val POOL = "POOLSWAP".toByteArray()
    val NPOOL = "NPOOLSWA".toByteArray()
    val FARM = "FARMSWAP".toByteArray()
    val UFARM = "UFARMSWA".toByteArray()
    val LOTTERY = "LOTTERY!".toByteArray()
    val LOTENTRY = "LOTENTRY".toByteArray()
    val REGISTRY = "REGISTRY".toByteArray()
    val MLBRAIN = "MLBRAIN!".toByteArray()
    val CLPOOL = "CLPOOL!!".toByteArray()
    val CLPOS = "CLPOSIT!".toByteArray()
    val BOOK = "ORDERBOK".toByteArray()
    val AIFEE = "AIFEE!!!".toByteArray()
    val THMETA = "THMETA!!".toByteArray()
    val GOVPROP = "GOVPROP!".toByteArray()
    val GOVVOTE = "GOVVOTE!".toByteArray()
    val GPOOLS = "GPVOOLS!".toByteArray()
    val VPCLAIM = "VPCLAIM!".toByteArray()
    val FARMSTATE = "FARMSTAT".toByteArray()
}

// ============================================================================
// Error Codes
// ============================================================================

/**
 * Error codes returned by the AeX402 program.
 * Matches the error definitions in aex402.c.
 */
enum class AeX402Error(val code: Int, val message: String) {
    PAUSED(6000, "Pool is paused"),
    INVALID_AMP(6001, "Invalid amplification coefficient"),
    MATH_OVERFLOW(6002, "Math overflow"),
    ZERO_AMOUNT(6003, "Zero amount"),
    SLIPPAGE_EXCEEDED(6004, "Slippage exceeded"),
    INVALID_INVARIANT(6005, "Invalid invariant or PDA mismatch"),
    INSUFFICIENT_LIQUIDITY(6006, "Insufficient liquidity"),
    VAULT_MISMATCH(6007, "Vault mismatch"),
    EXPIRED(6008, "Expired or ended"),
    ALREADY_INITIALIZED(6009, "Already initialized"),
    UNAUTHORIZED(6010, "Unauthorized"),
    RAMP_CONSTRAINT(6011, "Ramp constraint violated"),
    LOCKED(6012, "Tokens are locked"),
    FARMING_ERROR(6013, "Farming error"),
    INVALID_OWNER(6014, "Invalid account owner"),
    INVALID_DISCRIMINATOR(6015, "Invalid account discriminator"),
    CPI_FAILED(6016, "CPI call failed"),
    FULL(6017, "Orderbook/registry is full"),
    CIRCUIT_BREAKER(6018, "Circuit breaker triggered"),
    ORACLE_ERROR(6019, "Oracle price validation failed"),
    RATE_LIMIT(6020, "Rate limit exceeded"),
    GOVERNANCE_ERROR(6021, "Governance error"),
    ORDER_ERROR(6022, "Orderbook error"),
    TICK_ERROR(6023, "Invalid tick"),
    RANGE_ERROR(6024, "Invalid price range"),
    FLASH_ERROR(6025, "Flash loan error"),
    COOLDOWN(6026, "Cooldown period not elapsed"),
    MEV_PROTECTION(6027, "MEV protection triggered"),
    STALE_DATA(6028, "Stale data"),
    BIAS_ERROR(6029, "ML bias error"),
    DURATION_ERROR(6030, "Invalid duration");

    companion object {
        fun fromCode(code: Int): AeX402Error? = entries.find { it.code == code }
    }
}

// ============================================================================
// TWAP Windows
// ============================================================================

/**
 * Time windows for TWAP oracle queries.
 */
enum class TwapWindow(val value: Int) {
    HOUR_1(0),
    HOUR_4(1),
    HOUR_24(2),
    DAY_7(3);
}

// ============================================================================
// Circuit Breaker Constants
// ============================================================================

const val CB_PRICE_DEV_BPS = 1000      // 10% price deviation triggers
const val CB_VOLUME_MULT = 10          // Volume > 10x avg triggers
const val CB_COOLDOWN_SLOTS = 9000     // ~1 hour cooldown
const val CB_AUTO_RESUME_SLOTS = 54000 // 6 hour auto-resume

// ============================================================================
// Rate Limiting Constants
// ============================================================================

const val RL_SLOTS_PER_EPOCH = 750     // ~5 minute epochs

// ============================================================================
// Governance Constants
// ============================================================================

const val GOV_VOTE_SLOTS = 518400      // ~3 days voting
const val GOV_TIMELOCK_SLOTS = 172800  // ~1 day execution delay
const val GOV_QUORUM_BPS = 1000        // 10% of LP supply must vote
const val GOV_THRESHOLD_BPS = 5000     // 50%+ of votes to pass

// ============================================================================
// ML Brain Constants
// ============================================================================

const val ML_GAMMA = 0.9        // Discount factor
const val ML_ALPHA = 0.1        // Learning rate
const val ML_EPSILON = 0.1      // Exploration rate
const val ML_NUM_STATES = 27    // 3^3 states
const val ML_NUM_ACTIONS = 9    // 9 possible actions
