package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "cash_movements")
data class CashMovement(
    @PrimaryKey val id: String,
    val shiftId: Long,
    val type: CashMovementType,
    val amount: BigDecimal,
    val reason: String,
    val staffId: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CashMovementType {
    FLOAT,
    CASH_DROP,
    EXPENSE,
    SAFE_DROP,
    REFUND,
    PAYMENT_CORRECTION
}
