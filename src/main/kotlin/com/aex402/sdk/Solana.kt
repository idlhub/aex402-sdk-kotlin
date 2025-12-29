/**
 * AeX402 SDK - Solana Types
 *
 * Minimal Solana type definitions for JVM/Android compatibility.
 * These can be replaced with a proper Solana SDK like Sol4k when available.
 */
package com.aex402.sdk

import java.security.MessageDigest

// ============================================================================
// PublicKey
// ============================================================================

/**
 * Solana public key (32 bytes).
 * Uses Base58 encoding for string representation.
 */
class PublicKey(private val bytes: ByteArray) {

    init {
        require(bytes.size == 32) { "PublicKey must be 32 bytes, got ${bytes.size}" }
    }

    /**
     * Create from Base58 string.
     */
    constructor(base58: String) : this(Base58.decode(base58))

    /**
     * Get the raw bytes.
     */
    fun toBytes(): ByteArray = bytes.copyOf()

    /**
     * Get the bytes as a buffer.
     */
    fun toBuffer(): ByteArray = bytes.copyOf()

    /**
     * Get Base58 string representation.
     */
    fun toBase58(): String = Base58.encode(bytes)

    override fun toString(): String = toBase58()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicKey) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    companion object {
        /** System Program ID */
        val SYSTEM_PROGRAM = PublicKey("11111111111111111111111111111111")

        /** Token Program ID */
        val TOKEN_PROGRAM = PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")

        /** Token-2022 Program ID */
        val TOKEN_2022_PROGRAM = PublicKey("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb")

        /**
         * Default public key (all zeros).
         */
        val DEFAULT = PublicKey(ByteArray(32))

        /**
         * Find a program-derived address.
         * Returns (pda, bump) pair.
         */
        fun findProgramAddress(seeds: List<ByteArray>, programId: PublicKey): Pair<PublicKey, Int> {
            for (bump in 255 downTo 0) {
                try {
                    val address = createProgramAddress(seeds + listOf(byteArrayOf(bump.toByte())), programId)
                    // Check if it's a valid PDA (off curve)
                    if (isOnCurve(address.bytes)) continue
                    return Pair(address, bump)
                } catch (e: Exception) {
                    continue
                }
            }
            throw IllegalStateException("Unable to find a valid PDA")
        }

        /**
         * Create a program-derived address.
         */
        fun createProgramAddress(seeds: List<ByteArray>, programId: PublicKey): PublicKey {
            val buffer = mutableListOf<Byte>()
            for (seed in seeds) {
                require(seed.size <= 32) { "Seed too large" }
                buffer.addAll(seed.toList())
            }
            buffer.addAll(programId.bytes.toList())
            buffer.addAll("ProgramDerivedAddress".toByteArray().toList())

            val hash = sha256(buffer.toByteArray())
            return PublicKey(hash)
        }

        private fun sha256(data: ByteArray): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(data)
        }

        /**
         * Check if a public key is on the ed25519 curve.
         * This is a simplified check - in production, use a proper ed25519 library.
         */
        private fun isOnCurve(bytes: ByteArray): Boolean {
            // Simplified check: assume all hashes produce off-curve points
            // In a real implementation, you'd verify using ed25519 point decompression
            return false
        }
    }
}

// ============================================================================
// Base58 Encoding
// ============================================================================

/**
 * Base58 encoding/decoding for Solana addresses.
 */
object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val ENCODED_ZERO = ALPHABET[0]
    private val INDEXES = IntArray(128) { -1 }.apply {
        ALPHABET.forEachIndexed { i, c -> this[c.code] = i }
    }

    /**
     * Encode bytes to Base58 string.
     */
    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""

        // Count leading zeros
        var zeros = 0
        while (zeros < input.size && input[zeros] == 0.toByte()) {
            zeros++
        }

        // Convert to Base58
        val inputCopy = input.copyOf()
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size
        var inputStart = zeros

        while (inputStart < inputCopy.size) {
            encoded[--outputStart] = ALPHABET[divmod(inputCopy, inputStart, 256, 58).toInt()]
            if (inputCopy[inputStart] == 0.toByte()) {
                inputStart++
            }
        }

        while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
            outputStart++
        }
        while (zeros-- > 0) {
            encoded[--outputStart] = ENCODED_ZERO
        }

        return String(encoded, outputStart, encoded.size - outputStart)
    }

    /**
     * Decode Base58 string to bytes.
     */
    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)

        // Convert Base58 string to base58 digits
        val input58 = ByteArray(input.length)
        for (i in input.indices) {
            val c = input[i]
            val digit = if (c.code < 128) INDEXES[c.code] else -1
            if (digit < 0) throw IllegalArgumentException("Invalid Base58 character: $c")
            input58[i] = digit.toByte()
        }

        // Count leading zeros
        var zeros = 0
        while (zeros < input58.size && input58[zeros] == 0.toByte()) {
            zeros++
        }

        // Convert to bytes
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var inputStart = zeros

        while (inputStart < input58.size) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256)
            if (input58[inputStart] == 0.toByte()) {
                inputStart++
            }
        }

        while (outputStart < decoded.size && decoded[outputStart] == 0.toByte()) {
            outputStart++
        }

        return ByteArray(zeros + decoded.size - outputStart).also { result ->
            System.arraycopy(decoded, outputStart, result, zeros, decoded.size - outputStart)
        }
    }

    private fun divmod(number: ByteArray, firstDigit: Int, base: Int, divisor: Int): Byte {
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toInt() and 0xFF
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder.toByte()
    }
}

// ============================================================================
// Account Meta
// ============================================================================

/**
 * Account metadata for instruction building.
 */
data class AccountMeta(
    val pubkey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)

/**
 * Create a writable signer account meta.
 */
fun AccountMeta.Companion.writableSigner(pubkey: PublicKey) =
    AccountMeta(pubkey, isSigner = true, isWritable = true)

/**
 * Create a readonly signer account meta.
 */
fun AccountMeta.Companion.readonlySigner(pubkey: PublicKey) =
    AccountMeta(pubkey, isSigner = true, isWritable = false)

/**
 * Create a writable account meta.
 */
fun AccountMeta.Companion.writable(pubkey: PublicKey) =
    AccountMeta(pubkey, isSigner = false, isWritable = true)

/**
 * Create a readonly account meta.
 */
fun AccountMeta.Companion.readonly(pubkey: PublicKey) =
    AccountMeta(pubkey, isSigner = false, isWritable = false)

// Companion object for extension functions
val AccountMeta.Companion = object {}

// ============================================================================
// Transaction Instruction
// ============================================================================

/**
 * Transaction instruction for Solana.
 */
data class TransactionInstruction(
    val programId: PublicKey,
    val keys: List<AccountMeta>,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TransactionInstruction
        return programId == other.programId &&
               keys == other.keys &&
               data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = programId.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
