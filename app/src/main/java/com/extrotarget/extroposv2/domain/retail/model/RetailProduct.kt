package com.extrotarget.extroposv2.domain.retail.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.domain.commerce.CommerceItem
import java.math.BigDecimal

@Entity(tableName = "retail_products")
data class RetailProduct(
    @PrimaryKey override val id: String,
    override val name: String,
    override val basePrice: BigDecimal,
    val sku: String,
    val barcode: String?,
    val taxRate: BigDecimal,
    val stockQuantity: BigDecimal,
    val minStockLevel: BigDecimal = BigDecimal.ZERO,
    val categoryId: String?,
    val supplierId: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
) : CommerceItem
