package com.extrotarget.extroposv2.core.data.model.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "stock_transfers")
data class StockTransfer(
    @PrimaryKey val id: String,
    val fromBranchId: String,
    val toBranchId: String,
    val productId: String,
    val productName: String,
    val quantity: BigDecimal,
    val status: StockTransferStatus = StockTransferStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
)

enum class StockTransferStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
