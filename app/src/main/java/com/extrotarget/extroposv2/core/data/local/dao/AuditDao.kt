package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.extrotarget.extroposv2.core.data.model.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Insert
    suspend fun insertLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 1000")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE module = :module ORDER BY timestamp DESC")
    fun getLogsByModule(module: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE severity = 'CRITICAL' OR severity = 'WARNING' ORDER BY timestamp DESC")
    fun getAlertLogs(): Flow<List<AuditLog>>

    @Query("""
        SELECT * FROM audit_logs 
        WHERE (:module IS NULL OR module = :module)
        AND (:staffId IS NULL OR staffId = :staffId)
        AND (timestamp BETWEEN :startTime AND :endTime)
        ORDER BY timestamp DESC
    """)
    fun getFilteredLogs(
        module: String?,
        staffId: String?,
        startTime: Long,
        endTime: Long
    ): Flow<List<AuditLog>>

    @Query("SELECT DISTINCT staffName FROM audit_logs")
    fun getStaffNames(): Flow<List<String>>

    @Query("SELECT DISTINCT staffId FROM audit_logs")
    fun getStaffIds(): Flow<List<String>>

    @Query("SELECT staffId, staffName FROM audit_logs GROUP BY staffId")
    fun getStaffMembers(): Flow<List<StaffSummary>>

    data class StaffSummary(val staffId: String, val staffName: String)
}
