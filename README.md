# AeX402 Kotlin SDK

Kotlin SDK for the AeX402 Hybrid AMM on Solana.

## Features

- **Constants**: Program ID, discriminators, error codes
- **Data Classes**: Pool, NPool, Farm, UserFarm, Lottery, and more
- **Account Parsing**: Deserialize on-chain account data
- **Instruction Builders**: Build transaction instructions for all handlers
- **Math Functions**: StableSwap math using Newton's method
- **PDA Derivation**: Derive program addresses
- **Async Client**: High-level client with coroutines

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.aex402:aex402-sdk:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.aex402:aex402-sdk:1.0.0'
}
```

## Quick Start

```kotlin
import com.aex402.sdk.*
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

fun main() = runBlocking {
    // Create client
    val client = createDevnetClient()

    // Fetch pool
    val poolAddress = PublicKey("YourPoolAddress...")
    val pool = client.getPool(poolAddress)

    if (pool != null) {
        println("Pool TVL: ${pool.bal0} / ${pool.bal1}")
        println("Fee: ${pool.feeBps} bps")
        println("Amp: ${pool.amp}")
    }

    // Simulate swap
    val amountIn = BigInteger.valueOf(1_000_000_000L) // 1 token
    val expectedOut = client.simulateSwap(
        poolAddress,
        amountIn,
        SwapDirection.TOKEN0_TO_TOKEN1
    )
    println("Expected output: $expectedOut")

    // Get pool stats
    val stats = client.getPoolStats(poolAddress)
    println("Virtual price: ${stats?.virtualPrice}")
}
```

## Usage

### Math Operations

```kotlin
import com.aex402.sdk.*
import java.math.BigInteger

// Calculate invariant D
val d = calcD(
    x = BigInteger.valueOf(1_000_000_000_000L),
    y = BigInteger.valueOf(1_000_000_000_000L),
    amp = BigInteger.valueOf(100)
)
println("Invariant D: $d")

// Simulate swap
val amountOut = simulateSwap(
    balIn = BigInteger.valueOf(1_000_000_000_000L),
    balOut = BigInteger.valueOf(1_000_000_000_000L),
    amountIn = BigInteger.valueOf(1_000_000_000L),
    amp = BigInteger.valueOf(100),
    feeBps = BigInteger.valueOf(30)
)
println("Output: $amountOut")

// Calculate LP tokens
val lpTokens = calcLpTokens(
    amt0 = BigInteger.valueOf(1_000_000_000L),
    amt1 = BigInteger.valueOf(1_000_000_000L),
    bal0 = BigInteger.valueOf(100_000_000_000L),
    bal1 = BigInteger.valueOf(100_000_000_000L),
    lpSupply = BigInteger.valueOf(100_000_000_000L),
    amp = BigInteger.valueOf(100)
)
println("LP tokens: $lpTokens")
```

### Building Instructions

```kotlin
import com.aex402.sdk.*
import java.math.BigInteger

val pool = PublicKey("PoolAddress...")
val vault0 = PublicKey("Vault0...")
val vault1 = PublicKey("Vault1...")
val userToken0 = PublicKey("UserToken0...")
val userToken1 = PublicKey("UserToken1...")
val user = PublicKey("User...")

// Create swap instruction
val swapIx = swapT0T1Instruction(
    pool = pool,
    vault0 = vault0,
    vault1 = vault1,
    userToken0 = userToken0,
    userToken1 = userToken1,
    user = user,
    args = SwapSimpleArgs(
        amountIn = BigInteger.valueOf(1_000_000_000L),
        minOut = BigInteger.valueOf(990_000_000L)
    )
)

// Create add liquidity instruction
val addLiqIx = addLiquidityInstruction(
    pool = pool,
    vault0 = vault0,
    vault1 = vault1,
    lpMint = PublicKey("LpMint..."),
    userToken0 = userToken0,
    userToken1 = userToken1,
    userLp = PublicKey("UserLp..."),
    user = user,
    args = AddLiqArgs(
        amount0 = BigInteger.valueOf(1_000_000_000L),
        amount1 = BigInteger.valueOf(1_000_000_000L),
        minLp = BigInteger.valueOf(1_000_000_000L)
    )
)
```

### Parsing Account Data

```kotlin
import com.aex402.sdk.*

// Parse raw account data
val rawData: ByteArray = // ... fetch from RPC
val pool = parsePool(rawData)

if (pool != null) {
    println("Authority: ${pool.authority}")
    println("Mint0: ${pool.mint0}")
    println("Balance0: ${pool.bal0}")
    println("Paused: ${pool.paused}")
}

// Decode TWAP result
val twapResult = decodeTwapResult(BigInteger.valueOf(0x12345678))
println("TWAP price: ${twapResult.priceAsFloat()}")
println("Confidence: ${twapResult.confidencePercent()}%")
```

### PDA Derivation

```kotlin
import com.aex402.sdk.*

val mint0 = PublicKey("TokenMint0...")
val mint1 = PublicKey("TokenMint1...")

// Derive pool address
val (poolPda, bump) = derivePoolPda(mint0, mint1)
println("Pool PDA: $poolPda, bump: $bump")

// Derive farm address
val (farmPda, farmBump) = deriveFarmPda(poolPda)
println("Farm PDA: $farmPda")

// Derive user farm address
val user = PublicKey("User...")
val (userFarmPda, _) = deriveUserFarmPda(farmPda, user)
println("User Farm PDA: $userFarmPda")
```

## Constants

```kotlin
// Program ID
val programId = PROGRAM_ID // "3AMM53MsJZy2Jvf7PeHHga3bsGjWV4TSaYz29WUtcdje"

// Pool constants
val minAmp = MIN_AMP       // 1
val maxAmp = MAX_AMP       // 100000
val defaultFee = DEFAULT_FEE_BPS // 30 bps
val newtonIterations = NEWTON_ITERATIONS // 255

// Error codes
val error = AeX402Error.SLIPPAGE_EXCEEDED
println("${error.code}: ${error.message}")
```

## Error Handling

```kotlin
import com.aex402.sdk.*

// Parse error code from transaction
val errorCode = 6004
val error = AeX402Error.fromCode(errorCode)
if (error != null) {
    println("Error: ${error.name} - ${error.message}")
} else {
    println("Unknown error code: $errorCode")
}
```

## Architecture

```
com.aex402.sdk/
├── Constants.kt    - Program ID, discriminators, error codes
├── Types.kt        - Data classes for accounts and args
├── Solana.kt       - Minimal Solana types (PublicKey, Base58)
├── Math.kt         - StableSwap math (Newton's method)
├── Accounts.kt     - Account parsing and PDA derivation
├── Instructions.kt - Instruction builders
└── Client.kt       - High-level async client
```

## Requirements

- JDK 11+
- Kotlin 1.9+
- kotlinx-coroutines 1.7+
- kotlinx-serialization 1.6+
- OkHttp 4.12+

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## License

MIT License
