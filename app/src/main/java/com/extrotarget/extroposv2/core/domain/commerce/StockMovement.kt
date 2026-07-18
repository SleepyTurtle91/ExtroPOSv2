package com.extrotarget.extroposv2.core.domain.commerce

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
    indices = [Index("productId"), Index("timestamp")]
)
data class StockMovement(
    @PrimaryKey val id: String,
    val productId: String,
    val type: StockMovementType,
    val quantity: BigDecimal,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String? = null,
    val createdBy: String, // Staff ID
    val referenceId: String? = null, // Sale ID, PO ID, etc.
    val deviceId: String? = "TERM-01"
)

enum class StockMovementType {
    SALE,
    RESTOCK,
    ADJUSTMENT,
    RETURN,
    TRANSFER
}
