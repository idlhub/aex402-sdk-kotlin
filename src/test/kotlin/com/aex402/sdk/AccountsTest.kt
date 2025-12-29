package com.aex402.sdk

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AccountsTest {

    @Test
    fun `parsePool returns null for too small data`() {
        val data = ByteArray(100)
        assertNull(parsePool(data))
    }

    @Test
    fun `parsePool returns null for wrong discriminator`() {
        val data = ByteArray(1024)
        "WRONGDSC".toByteArray().copyInto(data, 0)
        assertNull(parsePool(data))
    }

    @Test
    fun `parsePool parses valid data correctly`() {
        val data = buildPoolData()
        val pool = parsePool(data)

        assertNotNull(pool)
        assertEquals(BigInteger.valueOf(100), pool!!.amp)
        assertEquals(BigInteger.valueOf(30), pool.feeBps)
        assertEquals(BigInteger.valueOf(1000000000000), pool.bal0)
        assertEquals(BigInteger.valueOf(1000000000000), pool.bal1)
        assertFalse(pool.paused)
        assertEquals(42, pool.bump)
    }

    @Test
    fun `parseFarm returns null for wrong discriminator`() {
        val data = ByteArray(120)
        "WRONGDSC".toByteArray().copyInto(data, 0)
        assertNull(parseFarm(data))
    }

    @Test
    fun `parseFarm parses valid data correctly`() {
        val data = buildFarmData()
        val farm = parseFarm(data)

        assertNotNull(farm)
        assertEquals(BigInteger.valueOf(1000000), farm!!.rewardRate)
        assertEquals(1704067200L, farm.startTime)
        assertEquals(1704153600L, farm.endTime)
    }

    @Test
    fun `parseUserFarm returns null for wrong discriminator`() {
        val data = ByteArray(96)
        "WRONGDSC".toByteArray().copyInto(data, 0)
        assertNull(parseUserFarm(data))
    }

    @Test
    fun `parseUserFarm parses valid data correctly`() {
        val data = buildUserFarmData()
        val userFarm = parseUserFarm(data)

        assertNotNull(userFarm)
        assertEquals(BigInteger.valueOf(500000000000), userFarm!!.staked)
        assertEquals(BigInteger.valueOf(1000000), userFarm.rewardDebt)
        assertEquals(1704067200L, userFarm.lockEnd)
    }

    @Test
    fun `parseLottery returns null for wrong discriminator`() {
        val data = ByteArray(152)
        "WRONGDSC".toByteArray().copyInto(data, 0)
        assertNull(parseLottery(data))
    }

    @Test
    fun `parseLottery parses valid data correctly`() {
        val data = buildLotteryData()
        val lottery = parseLottery(data)

        assertNotNull(lottery)
        assertEquals(BigInteger.valueOf(1000000000), lottery!!.ticketPrice)
        assertEquals(BigInteger.valueOf(100), lottery.totalTickets)
        assertFalse(lottery.drawn)
        assertFalse(lottery.claimed)
    }

    @Test
    fun `derivePoolPda returns valid PDA`() {
        val mint0 = PublicKey(ByteArray(32) { 1 })
        val mint1 = PublicKey(ByteArray(32) { 2 })

        val (pda, bump) = derivePoolPda(mint0, mint1)

        assertNotNull(pda)
        assertTrue(bump in 0..255)
    }

    @Test
    fun `deriveFarmPda returns valid PDA`() {
        val pool = PublicKey(ByteArray(32) { 3 })

        val (pda, bump) = deriveFarmPda(pool)

        assertNotNull(pda)
        assertTrue(bump in 0..255)
    }

    @Test
    fun `deriveUserFarmPda returns valid PDA`() {
        val farm = PublicKey(ByteArray(32) { 4 })
        val user = PublicKey(ByteArray(32) { 5 })

        val (pda, bump) = deriveUserFarmPda(farm, user)

        assertNotNull(pda)
        assertTrue(bump in 0..255)
    }

    @Test
    fun `deriveLotteryPda returns valid PDA`() {
        val pool = PublicKey(ByteArray(32) { 6 })

        val (pda, bump) = deriveLotteryPda(pool)

        assertNotNull(pda)
        assertTrue(bump in 0..255)
    }

    @Test
    fun `deriveGlobalVPoolPda returns valid PDA`() {
        val (pda, bump) = deriveGlobalVPoolPda()

        assertNotNull(pda)
        assertTrue(bump in 0..255)
    }

    // ========== Helper Functions ==========

    private fun buildPoolData(): ByteArray {
        val buffer = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN)

        // Discriminator
        buffer.put("POOLSWAP".toByteArray())

        // Pubkeys (6 * 32 = 192 bytes)
        repeat(6) { buffer.put(ByteArray(32) { (it + 1).toByte() }) }

        // Amp fields
        buffer.putLong(100)  // amp
        buffer.putLong(100)  // initAmp
        buffer.putLong(200)  // targetAmp
        buffer.putLong(1704067200)  // rampStart
        buffer.putLong(1704153600)  // rampStop

        // Fee fields
        buffer.putLong(30)   // feeBps
        buffer.putLong(50)   // adminFeePct

        // Balance fields
        buffer.putLong(1000000000000)  // bal0
        buffer.putLong(1000000000000)  // bal1
        buffer.putLong(1000000000000)  // lpSupply
        buffer.putLong(1000000)        // adminFee0
        buffer.putLong(1000000)        // adminFee1

        // Volume fields
        buffer.putLong(5000000000000)  // vol0
        buffer.putLong(5000000000000)  // vol1

        // Flags
        buffer.put(0)   // paused
        buffer.put(42)  // bump
        buffer.put(43)  // vault0Bump
        buffer.put(44)  // vault1Bump
        buffer.put(45)  // lpMintBump
        buffer.put(ByteArray(3))  // _pad

        // Pending authority
        buffer.put(ByteArray(32))  // pendingAuth
        buffer.putLong(0)          // authTime

        // Pending amp
        buffer.putLong(0)  // pendingAmp
        buffer.putLong(0)  // ampTime

        // Analytics
        buffer.putLong(1000)  // tradeCount
        buffer.putLong(100000000000)  // tradeSum
        buffer.putInt(1000000)  // maxPrice
        buffer.putInt(900000)   // minPrice
        buffer.putInt(1000)     // hourSlot
        buffer.putInt(10)       // daySlot
        buffer.put(0)   // hourIdx
        buffer.put(0)   // dayIdx
        buffer.put(ByteArray(6))  // _pad2

        // Bloom filter
        buffer.put(ByteArray(128))

        // Hourly candles (24 * 12)
        repeat(24) {
            buffer.putInt(1000000)  // open
            buffer.putShort(1000)   // highD
            buffer.putShort(1000)   // lowD
            buffer.putShort(0)      // closeD
            buffer.putShort(1000)   // volume
        }

        // Daily candles (7 * 12)
        repeat(7) {
            buffer.putInt(1000000)
            buffer.putShort(1000)
            buffer.putShort(1000)
            buffer.putShort(0)
            buffer.putShort(1000)
        }

        return buffer.array()
    }

    private fun buildFarmData(): ByteArray {
        val buffer = ByteBuffer.allocate(120).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("FARMSWAP".toByteArray())
        buffer.put(ByteArray(32))  // pool
        buffer.put(ByteArray(32))  // rewardMint
        buffer.putLong(1000000)    // rewardRate
        buffer.putLong(1704067200) // startTime
        buffer.putLong(1704153600) // endTime
        buffer.putLong(500000000000) // totalStaked
        buffer.putLong(1000000000)   // accReward
        buffer.putLong(1704067200)   // lastUpdate

        return buffer.array()
    }

    private fun buildUserFarmData(): ByteArray {
        val buffer = ByteBuffer.allocate(96).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("UFARMSWA".toByteArray())
        buffer.put(ByteArray(32))  // owner
        buffer.put(ByteArray(32))  // farm
        buffer.putLong(500000000000)  // staked
        buffer.putLong(1000000)       // rewardDebt
        buffer.putLong(1704067200)    // lockEnd

        return buffer.array()
    }

    private fun buildLotteryData(): ByteArray {
        val buffer = ByteBuffer.allocate(152).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("LOTTERY!".toByteArray())
        buffer.put(ByteArray(32))  // pool
        buffer.put(ByteArray(32))  // authority
        buffer.put(ByteArray(32))  // lotteryVault
        buffer.putLong(1000000000) // ticketPrice
        buffer.putLong(100)        // totalTickets
        buffer.putLong(100000000000) // prizePool
        buffer.putLong(1704153600)   // endTime
        buffer.putLong(0)            // winningTicket
        buffer.put(0)  // drawn
        buffer.put(0)  // claimed
        buffer.put(ByteArray(6))  // _pad

        return buffer.array()
    }
}
