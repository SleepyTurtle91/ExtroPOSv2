package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SaleItem(
    @PrimaryKey val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal,
    val taxAmount: BigDecimal,
    val serviceChargeRate: BigDecimal = BigDecimal.ZERO,
    val serviceChargeAmount: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val discountLabel: String? = null,
    val totalAmount: BigDecimal,
    val modifiers: String? = null, // Comma-separated or JSON string
    val assignedStaffId: String? = null,
    val assignedStaffName: String? = null,
    val printerTag: String = "KITCHEN", // Default to KITCHEN for F&B
    val status: String = "PENDING" // e.g., PENDING, PREPARING, READY, SERVED
)