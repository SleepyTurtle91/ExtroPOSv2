package com.extrotarget.extroposv2.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: BigDecimal,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val serviceChargeAmount: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val discountLabel: String? = null,
    val roundingAdjustment: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: String, // e.g., CASH, CARD
    val status: String = "COMPLETED", // e.g., COMPLETED, VOIDED, REFUNDED, PENDING
    val memberId: String? = null,
    val staffId: String? = null,
    val tableId: String? = null,
    val note: String? = null,
    val cardType: String? = null,
    val maskedPan: String? = null,
    val approvalCode: String? = null,
    val autoCountSyncStatus: String = "NOT_SYNCED", // NOT_SYNCED, SYNCED, FAILED
    val autoCountDocNo: String? = null,
    val localSyncStatus: String = "SYNCED", // SYNCED, PENDING (Slaves use PENDING for local-only sales)
    val branchId: String? = null
)
