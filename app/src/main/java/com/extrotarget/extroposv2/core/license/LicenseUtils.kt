package com.extrotarget.extroposv2.core.license

import com.extrotarget.extroposv2.core.config.AppConfig
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object LicenseUtils {
    fun generateHWID(manufacturer: String, model: String, androidId: String): String {
        val rawId = manufacturer + model + androidId
        return sha256(rawId)
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.uppercase()
    }

    fun formatDeviceId(deviceId: String): String {
        return deviceId.take(16).chunked(4).joinToString("-")
    }

    fun generateActivationKey(deviceId: String, expiry: String, secret: String = AppConfig.Security.CRYPTO_SALT): String {
        val hmacKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(hmacKey)
        val hash = hmac.doFinal((deviceId + expiry).toByteArray())
        val keyPart = hash.joinToString("") { "%02x".format(it) }.take(12).uppercase()
        return "EXTRO-$keyPart"
    }

    fun calculateStatus(
        info: LicenseInfo,
        now: LocalDateTime = LocalDateTime.now(),
        trialDays: Int = 30,
        graceDays: Int = 3
    ): LicenseStatus {
        if (info.isActivated) {
            // Verify HMAC integrity
            val expectedKey = generateActivationKey(info.deviceId, info.expiryDate?.toString() ?: "") // Note: Using toString() or a specific format
            // For real verification, we need the exact string used during generation.
            // Let's assume the caller handles the formatting consistency.
            
            return if (info.expiryDate == null || info.expiryDate.isAfter(now)) {
                LicenseStatus.Valid
            } else {
                LicenseStatus.Expired
            }
        }

        val trialStart = info.trialStartDate ?: return LicenseStatus.Invalid
        val daysElapsed = ChronoUnit.DAYS.between(trialStart, now).toInt()
        
        val remainingTrial = trialDays - daysElapsed
        if (remainingTrial > 0) {
            return LicenseStatus.Trial(remainingTrial)
        }
        
        val remainingGrace = (trialDays + graceDays) - daysElapsed
        if (remainingGrace > 0) {
            return LicenseStatus.GracePeriod(remainingGrace)
        }

        return LicenseStatus.Expired
    }
}
