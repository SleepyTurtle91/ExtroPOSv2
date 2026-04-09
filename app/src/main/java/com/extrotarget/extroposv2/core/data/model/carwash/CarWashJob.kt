package com.extrotarget.extroposv2.core.data.model.carwash

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "car_wash_jobs")
data class CarWashJob(
    @PrimaryKey val id: String,
    val plateNumber: String,
    val carModel: String? = null,
    val serviceName: String,
    val price: BigDecimal,
    val assignedStaffId: String? = null,
    val assignedStaffName: String? = null,
    val status: CarWashStatus = CarWashStatus.QUEUED,
    val startTime: Long = System.currentTimeMillis(),
    val completionTime: Long? = null,
    val notes: String? = null
)

enum class CarWashStatus {
    QUEUED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    ARCHIVED
}