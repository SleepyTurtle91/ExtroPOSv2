package com.extrotarget.extroposv2.core.data.model.fnb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fnb_tables")
data class Table(
    @PrimaryKey val id: String,
    val name: String,
    val status: TableStatus = TableStatus.AVAILABLE,
    val capacity: Int = 4,
    val zone: String = "Indoor", // e.g., "Indoor", "Outdoor", "VIP"
    val currentSaleId: String? = null,
    val hasUnsentItems: Boolean = false // New: Track if any items in the current cart are not yet sent to kitchen
)

enum class TableStatus {
    AVAILABLE,
    OCCUPIED,
    BILLING,
    RESERVED,
    DIRTY
}