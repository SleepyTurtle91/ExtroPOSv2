package com.extrotarget.extroposv2.core.data.model.carwash

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.data.model.Sale
import java.math.BigDecimal

@Entity(
    tableName = "commission_records",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("staffId"), Index("saleId")]
)
data class CommissionRecord(
    @PrimaryKey val id: String,
    val staffId: String,
    val saleId: String,
    val serviceName: String,
    val servicePrice: BigDecimal,
    val commissionRate: BigDecimal, // Percentage (Ci)
    val fixedAllowance: BigDecimal, // Fixed fee (Fi)
    val calculatedCommission: BigDecimal, // (Pi * Ci) + Fi
    val timestamp: Long = System.currentTimeMillis()
)