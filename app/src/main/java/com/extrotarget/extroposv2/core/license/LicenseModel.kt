package com.extrotarget.extroposv2.core.license

import java.time.LocalDateTime

data class LicenseInfo(
    val deviceId: String,
    val activationKey: String? = null,
    val isActivated: Boolean = false,
    val expiryDate: LocalDateTime? = null,
    val trialStartDate: LocalDateTime? = null,
    val licenseType: LicenseType = LicenseType.TRIAL
)

enum class LicenseType {
    TRIAL,
    PRO,
    ENTERPRISE
}

sealed class LicenseStatus {
    object Valid : LicenseStatus()
    data class Trial(val daysRemaining: Int) : LicenseStatus()
    object Expired : LicenseStatus()
    object Invalid : LicenseStatus()
}
