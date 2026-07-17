package com.extrotarget.extroposv2.domain.retail.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "purchase_orders",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("supplierId")]
)
data class PurchaseOrder(
    @PrimaryKey val id: String,
    val poNumber: String, // e.g., "PO-2024-0001"
    val supplierId: String,
    val status: WorkflowStatus = WorkflowStatus.PENDING,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val createdAt: Long = System.currentTimeMillis(),
    val receivedAt: Long? = null,
    val createdBy: String,
    val receivedBy: String? = null,
    val note: String? = null
)

@Entity(
    tableName = "purchase_order_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseOrder::class,
            parentColumns = ["id"],
            childColumns = ["poId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("poId"), Index("productId")]
)
data class PurchaseOrderItem(
    @PrimaryKey val id: String,
    val poId: String,
    val productId: String,
    val quantity: BigDecimal,
    val unitCost: BigDecimal,
    val receivedQuantity: BigDecimal = BigDecimal.ZERO
)
