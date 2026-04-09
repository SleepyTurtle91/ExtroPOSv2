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
    val note: String? = null,
    val items: List<LaundryItem> = emptyList()
)

data class LaundryItem(
    val name: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val isWeightBased: Boolean
) {
    val totalPrice: BigDecimal = quantity.multiply(unitPrice)
}

enum class LaundryStatus {
    RECEIVED,
    PROCESSING,
    READY,
    COLLECTED
}