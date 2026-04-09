package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val sku: String,
    val barcode: String?,
    val price: BigDecimal,
    val taxRate: BigDecimal,
    val stockQuantity: BigDecimal,
    val minStockLevel: BigDecimal = BigDecimal.ZERO, // For low stock alerts
    val commissionRate: BigDecimal = BigDecimal.ZERO, // Percentage (e.g., 10.00 for 10%)
    val fixedCommission: BigDecimal = BigDecimal.ZERO, // Fixed fee per job
    val categoryId: String?,
    val description: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val printerTag: String? = null, // e.g., "KITCHEN", "BAR", "GENERAL"
    val isWeightBased: Boolean = false // If true, price is per KG, else per piece
)