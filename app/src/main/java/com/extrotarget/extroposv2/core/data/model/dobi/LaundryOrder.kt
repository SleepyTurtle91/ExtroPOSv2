package com.extrotarget.extroposv2.core.data.model.dobi

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "laundry_orders")
data class LaundryOrder(
    @PrimaryKey val id: String,
    val customerName: String,
    val customerPhone: String,
    val weightKg: BigDecimal,
    val totalPrice: BigDecimal,
    val status: LaundryStatus = LaundryStatus.RECEIVED,
    val receivedTimestamp: Long = System.currentTimeMillis(),
    val readyTimestamp: Long? = null,
    val collectedTimestamp: Long? = null,
    val note: String? = null
)

enum class LaundryStatus {
    RECEIVED,
    PROCESSING,
    READY,
    COLLECTED
}