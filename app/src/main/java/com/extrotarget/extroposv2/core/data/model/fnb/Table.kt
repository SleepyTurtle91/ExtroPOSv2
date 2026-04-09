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
    val currentSaleId: String? = null
)

enum class TableStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    DIRTY
}