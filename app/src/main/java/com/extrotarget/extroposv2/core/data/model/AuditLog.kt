package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val staffId: String,
    val staffName: String,
    val action: String, // e.g., VOID_ITEM, APPLY_DISCOUNT, OPEN_DRAWER, LOGIN
    val details: String, // JSON or descriptive string
    val module: String, // e.g., SALES, INVENTORY, SETTINGS
    val severity: String = "INFO" // INFO, WARNING, CRITICAL
)
