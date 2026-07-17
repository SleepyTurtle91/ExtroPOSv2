package com.extrotarget.extroposv2.core.license

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class LicenseUtilsTest {

    @Test
    fun testGenerateHWID() {
        val hwid = LicenseUtils.generateHWID("Samsung", "S21", "android123")
        assertNotNull(hwid)
        assertTrue(hwid.length == 64) // SHA-256 hex length
    }

    @Test
    fun testFormatDeviceId() {
        val hwid = "F4A792B1CC10D332" + "0".repeat(48)
        val formatted = LicenseUtils.formatDeviceId(hwid)
        assertEquals("F4A7-92B1-CC10-D332", formatted)
    }

    @Test
    fun testGenerateActivationKey() {
        val deviceId = "F4A792B1CC10D332"
        val expiry = "2025-01-01T00:00:00"
        val key = LicenseUtils.generateActivationKey(deviceId, expiry, "TEST_SALT")
        
        assertNotNull(key)
        assertTrue(key.startsWith("EXTRO-"))
        assertEquals(18, key.length) // "EXTRO-" (6) + 12 chars
    }

    @Test
    fun testCalculateStatusTrial() {
        val start = LocalDateTime.now().minusDays(10)
        val info = LicenseInfo(deviceId = "dev1", trialStartDate = start)
        val status = LicenseUtils.calculateStatus(info, now = LocalDateTime.now())
        
        assertTrue(status is LicenseStatus.Trial)
        assertEquals(20, (status as LicenseStatus.Trial).daysRemaining)
    }

    @Test
    fun testCalculateStatusGrace() {
        val start = LocalDateTime.now().minusDays(31)
        val info = LicenseInfo(deviceId = "dev1", trialStartDate = start)
        val status = LicenseUtils.calculateStatus(info, now = LocalDateTime.now())
        
        assertTrue(status is LicenseStatus.GracePeriod)
        assertEquals(2, (status as LicenseStatus.GracePeriod).daysRemaining)
    }

    @Test
    fun testCalculateStatusExpired() {
        val start = LocalDateTime.now().minusDays(34)
        val info = LicenseInfo(deviceId = "dev1", trialStartDate = start)
        val status = LicenseUtils.calculateStatus(info, now = LocalDateTime.now())
        
        assertEquals(LicenseStatus.Expired, status)
    }

    @Test
    fun testCalculateStatusActivated() {
        val expiry = LocalDateTime.now().plusDays(10)
        val info = LicenseInfo(deviceId = "dev1", isActivated = true, expiryDate = expiry)
        val status = LicenseUtils.calculateStatus(info, now = LocalDateTime.now())
        
        assertEquals(LicenseStatus.Valid, status)
    }
}
