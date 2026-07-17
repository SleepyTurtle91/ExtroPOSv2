package com.extrotarget.extroposv2.domain.fnb.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "fnb_modifier_groups")
data class ModifierGroup(
    @PrimaryKey val id: String,
    val name: String,
    val minSelect: Int = 0,
    val maxSelect: Int = 1,
    val isRequired: Boolean = false,
    val menuItemId: String // Links to MenuItem
)

@Entity(
    tableName = "fnb_modifier_options",
    foreignKeys = [
        ForeignKey(
            entity = ModifierGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class ModifierOption(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
    val priceAdjustment: BigDecimal = BigDecimal.ZERO,
    val isAvailable: Boolean = true
)
