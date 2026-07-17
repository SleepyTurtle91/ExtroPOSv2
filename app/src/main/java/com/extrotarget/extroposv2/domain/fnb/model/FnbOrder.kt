package com.extrotarget.extroposv2.domain.fnb.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import java.math.BigDecimal

@Entity(tableName = "fnb_orders")
data class FnbOrder(
    @PrimaryKey val id: String,
    val tableId: String?,
    val status: WorkflowStatus = WorkflowStatus.OPEN,
    val guestCount: Int = 1,
    val orderType: FnbOrderType = FnbOrderType.DINE_IN,
    val createdAt: Long = System.currentTimeMillis(),
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val staffId: String,
    val note: String? = null
)

@Entity(tableName = "fnb_order_items")
data class FnbOrderItem(
    @PrimaryKey val id: String,
    val orderId: String,
    val menuItemId: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val status: WorkflowStatus = WorkflowStatus.PENDING,
    val kitchenStation: String? = null,
    val notes: String? = null
)

enum class FnbOrderType {
    DINE_IN, TAKEAWAY, DELIVERY
}
