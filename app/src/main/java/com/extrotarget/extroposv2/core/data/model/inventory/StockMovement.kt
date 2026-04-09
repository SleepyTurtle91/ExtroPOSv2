package com.extrotarget.extroposv2.core.data.model.inventory

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.data.model.Product
import java.math.BigDecimal

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class StockMovement(
    @PrimaryKey val id: String,
    val productId: String,
    val quantity: BigDecimal, // Positive for addition, negative for reduction
    val type: String, // IN, OUT, ADJUSTMENT, SALE
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)