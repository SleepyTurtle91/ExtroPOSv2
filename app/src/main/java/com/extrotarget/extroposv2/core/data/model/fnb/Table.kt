package com.extrotarget.extroposv2.core.data.model.fnb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fnb_tables")
data class Table(
    @PrimaryKey val id: String,
    val code: String, // e.g., "T01", "A1"
    val name: String, // e.g., "Table 1", "Window Seat"
    val status: TableStatus = TableStatus.AVAILABLE,
    val capacity: Int = 4,
    val zone: String = "Indoor",
    val displayOrder: Int = 0,
    val currentSaleId: String? = null,
    val currentBillAmount: java.math.BigDecimal? = null,
    val hasUnsentItems: Boolean = false
)

enum class TableStatus {
    AVAILABLE,
    OCCUPIED,
    BILLING,
    RESERVED,
    DIRTY
}