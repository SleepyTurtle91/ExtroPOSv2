package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "modifiers")
data class Modifier(
    @PrimaryKey val id: String,
    val name: String,
    val priceAdjustment: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val isAvailable: Boolean = true // Added for "Out of Stock" grey-out support
)

@Entity(
    tableName = "modifier_links",
    foreignKeys = [
        ForeignKey(
            entity = Modifier::class,
            parentColumns = ["id"],
            childColumns = ["modifierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("modifierId"), Index("targetId")]
)
data class ModifierLink(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modifierId: String,
    val targetId: String, // Can be CategoryId or ProductId
    val targetType: ModifierTargetType
)

enum class ModifierTargetType {
    CATEGORY, PRODUCT
}
