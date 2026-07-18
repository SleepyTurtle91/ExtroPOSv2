package com.extrotarget.extroposv2.core.data.model.hardware

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "print_jobs")
data class PrintJob(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val printerId: String,
    val contentJson: String, // Serialized List<PrintCommand>
    val charWidth: Int = 32,
    val timestamp: Long = System.currentTimeMillis(),
    val status: PrintJobStatus = PrintJobStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null
)

enum class PrintJobStatus {
    PENDING, PRINTING, COMPLETED, FAILED
}
