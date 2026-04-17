package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "shift_adjustments",
    foreignKeys = [
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shiftId")]
)
data class ShiftAdjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shiftId: Long,
    val amount: BigDecimal, // Positive for Cash In, Negative for Cash Out
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: AdjustmentType,
    val staffName: String
)

enum class AdjustmentType {
    CASH_IN,
    CASH_OUT
}
