package com.extrotarget.extroposv2.domain.fnb.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.domain.commerce.CommerceItem
import java.math.BigDecimal

@Entity(tableName = "fnb_menu_items")
data class MenuItem(
    @PrimaryKey override val id: String,
    override val name: String,
    override val basePrice: BigDecimal,
    val taxRate: BigDecimal,
    val categoryId: String?,
    val kitchenStation: String?, // e.g., "KITCHEN", "BAR"
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val hasModifiers: Boolean = false
) : CommerceItem
