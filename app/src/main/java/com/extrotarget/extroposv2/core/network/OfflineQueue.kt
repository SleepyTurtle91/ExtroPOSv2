package com.extrotarget.extroposv2.core.network

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "offline_queue")
data class OfflineQueue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String = UUID.randomUUID().toString(),
    val serverReferenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val actionType: String, // e.g., "SALE", "STOCK_ADJUSTMENT", "AUDIT_LOG"
    val payloadJson: String,
    val retryCount: Int = 0,
    val status: QueueStatus = QueueStatus.PENDING,
    val lastError: String? = null
)

enum class QueueStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
