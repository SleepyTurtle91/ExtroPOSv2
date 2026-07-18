package com.extrotarget.extroposv2.core.util.audit

import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.data.local.dao.AuditDao
import com.extrotarget.extroposv2.core.data.model.AuditLog
import com.extrotarget.extroposv2.core.platform.DeviceIdentityManager
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditManager @Inject constructor(
    private val auditDao: AuditDao,
    private val sessionManager: SessionManager,
    private val deviceIdentityManager: DeviceIdentityManager
) {
    suspend fun logAction(
        action: String,
        details: String,
        module: String,
        severity: String = "INFO",
        oldValue: String? = null,
        newValue: String? = null,
        entityType: String? = null,
        entityId: String? = null
    ) {
        val currentStaff = sessionManager.getCurrentStaff()
        val identity = deviceIdentityManager.getDeviceIdentity()
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            staffId = currentStaff?.id ?: "SYSTEM",
            staffName = currentStaff?.name ?: "SYSTEM",
            action = action,
            details = details,
            module = module,
            severity = severity,
            oldValue = oldValue,
            newValue = newValue,
            entityType = entityType,
            entityId = entityId,
            deviceId = identity.terminalName
        )
        auditDao.insertLog(log)
    }
}
