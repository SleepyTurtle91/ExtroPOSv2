package com.extrotarget.extroposv2.core.util.audit

import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.data.local.dao.AuditDao
import com.extrotarget.extroposv2.core.data.model.AuditLog
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditManager @Inject constructor(
    private val auditDao: AuditDao,
    private val sessionManager: SessionManager
) {
    suspend fun logAction(
        action: String,
        details: String,
        module: String,
        severity: String = "INFO"
    ) {
        val currentStaff = sessionManager.getCurrentStaff()
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            staffId = currentStaff?.id ?: "SYSTEM",
            staffName = currentStaff?.name ?: "SYSTEM",
            action = action,
            details = details,
            module = module,
            severity = severity
        )
        auditDao.insertLog(log)
    }
}
