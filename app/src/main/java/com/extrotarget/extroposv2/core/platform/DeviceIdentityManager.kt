package com.extrotarget.extroposv2.core.platform

import com.extrotarget.extroposv2.core.util.security.SecurityManager
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdentityManager @Inject constructor(
    private val securityManager: SecurityManager
) {
    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_INSTALLATION_ID = "installation_id"
        private const val KEY_BRANCH_ID = "branch_id"
        private const val KEY_TERMINAL_NAME = "terminal_name"
        private const val KEY_REGISTERED_AT = "registered_at"
    }

    fun getDeviceIdentity(): DeviceIdentity {
        var deviceId = securityManager.getString(KEY_DEVICE_ID)
        var installationId = securityManager.getString(KEY_INSTALLATION_ID)
        val branchId = securityManager.getString(KEY_BRANCH_ID, "MAIN")!!
        val terminalName = securityManager.getString(KEY_TERMINAL_NAME, "TERMINAL-01")!!
        var registeredAt = securityManager.getString(KEY_REGISTERED_AT)?.toLongOrNull()

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            securityManager.saveString(KEY_DEVICE_ID, deviceId)
        }

        if (installationId == null) {
            installationId = UUID.randomUUID().toString()
            securityManager.saveString(KEY_INSTALLATION_ID, installationId)
        }

        if (registeredAt == null) {
            registeredAt = System.currentTimeMillis()
            securityManager.saveString(KEY_REGISTERED_AT, registeredAt.toString())
        }

        return DeviceIdentity(
            deviceId = deviceId,
            installationId = installationId,
            branchId = branchId,
            terminalName = terminalName,
            registeredAt = registeredAt
        )
    }

    fun updateIdentity(branchId: String, terminalName: String) {
        securityManager.saveString(KEY_BRANCH_ID, branchId)
        securityManager.saveString(KEY_TERMINAL_NAME, terminalName)
    }
}

data class DeviceIdentity(
    val deviceId: String,
    val installationId: String,
    val branchId: String,
    val terminalName: String,
    val registeredAt: Long
)
