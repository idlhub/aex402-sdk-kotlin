package com.aex402.sdk

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SolanaTest {

    @Test
    fun `PublicKey encodes and decodes correctly`() {
        val address = "3AMM53MsJZy2Jvf7PeHHga3bsGjWV4TSaYz29WUtcdje"
        val pubkey = PublicKey(address)

        assertEquals(address, pubkey.toBase58())
        assertEquals(32, pubkey.toBytes().size)
    }

    @Test
    fun `PublicKey from bytes round-trips correctly`() {
        val original = PublicKey(PROGRAM_ID)
        val bytes = original.toBytes()
        val restored = PublicKey(bytes)

        assertEquals(original, restored)
        assertEquals(original.toBase58(), restored.toBase58())
    }

    @Test
    fun `PublicKey equals works correctly`() {
        val pk1 = PublicKey(PROGRAM_ID)
        val pk2 = PublicKey(PROGRAM_ID)
        val pk3 = PublicKey(TOKEN_PROGRAM_ID)

        assertEquals(pk1, pk2)
        assertNotEquals(pk1, pk3)
    }

    @Test
    fun `PublicKey default is all zeros`() {
        val default = PublicKey.DEFAULT
        val bytes = default.toBytes()

        assertTrue(bytes.all { it == 0.toByte() })
    }

    @Test
    fun `Base58 encodes empty array to empty string`() {
        assertEquals("", Base58.encode(ByteArray(0)))
    }

    @Test
    fun `Base58 decodes empty string to empty array`() {
        assertArrayEquals(ByteArray(0), Base58.decode(""))
    }

    @Test
    fun `Base58 round-trips correctly`() {
        val original = ByteArray(32) { it.toByte() }
        val encoded = Base58.encode(original)
        val decoded = Base58.decode(encoded)

        assertArrayEquals(original, decoded)
    }

    @Test
    fun `Base58 handles leading zeros`() {
        val original = byteArrayOf(0, 0, 0, 1, 2, 3)
        val encoded = Base58.encode(original)
        val decoded = Base58.decode(encoded)

        assertArrayEquals(original, decoded)
    }

    @Test
    fun `AccountMeta has correct flags`() {
        val pubkey = PublicKey(PROGRAM_ID)

        val writableSigner = AccountMeta(pubkey, isSigner = true, isWritable = true)
        assertTrue(writableSigner.isSigner)
        assertTrue(writableSigner.isWritable)

        val readonlySigner = AccountMeta(pubkey, isSigner = true, isWritable = false)
        assertTrue(readonlySigner.isSigner)
        assertFalse(readonlySigner.isWritable)

        val writable = AccountMeta(pubkey, isSigner = false, isWritable = true)
        assertFalse(writable.isSigner)
        assertTrue(writable.isWritable)

        val readonly = AccountMeta(pubkey, isSigner = false, isWritable = false)
        assertFalse(readonly.isSigner)
        assertFalse(readonly.isWritable)
    }

    @Test
    fun `TransactionInstruction contains correct data`() {
        val programId = PublicKey(PROGRAM_ID)
        val account = AccountMeta(PublicKey(TOKEN_PROGRAM_ID), isSigner = false, isWritable = false)
        val data = byteArrayOf(1, 2, 3, 4)

        val ix = TransactionInstruction(programId, listOf(account), data)

        assertEquals(programId, ix.programId)
        assertEquals(1, ix.keys.size)
        assertArrayEquals(data, ix.data)
    }

    @Test
    fun `known Solana addresses decode correctly`() {
        // System Program
        val system = PublicKey("11111111111111111111111111111111")
        assertEquals(32, system.toBytes().size)
        assertTrue(system.toBytes().all { it == 0.toByte() })

        // Token Program
        val token = PublicKey(TOKEN_PROGRAM_ID)
        assertEquals(TOKEN_PROGRAM_ID, token.toBase58())

        // Token-2022 Program
        val token22 = PublicKey(TOKEN_2022_PROGRAM_ID)
        assertEquals(TOKEN_2022_PROGRAM_ID, token22.toBase58())
    }
}
