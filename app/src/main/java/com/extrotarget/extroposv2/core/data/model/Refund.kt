package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import java.math.BigDecimal

@Entity(tableName = "refunds")
data class Refund(
    @PrimaryKey val id: String,
    val saleId: String,
    val amount: BigDecimal,
    val status: WorkflowStatus = WorkflowStatus.PENDING,
    val reason: String,
    val requestedBy: String,
    val approvedBy: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val restockItems: Boolean = true
)
