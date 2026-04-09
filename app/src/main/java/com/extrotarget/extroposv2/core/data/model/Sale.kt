package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val discountLabel: String? = null,
    val roundingAdjustment: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: String, // e.g., CASH, CARD
    val status: String = "COMPLETED", // e.g., COMPLETED, VOIDED, REFUNDED
    val note: String? = null
)