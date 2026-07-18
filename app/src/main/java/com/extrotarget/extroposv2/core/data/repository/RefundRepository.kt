package com.extrotarget.extroposv2.core.data.repository

import androidx.room.withTransaction
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.RefundDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.core.data.model.Refund
import com.extrotarget.extroposv2.core.domain.commerce.StockMovement
import com.extrotarget.extroposv2.core.domain.commerce.StockMovementType
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefundRepository @Inject constructor(
    private val database: AppDatabase,
    private val refundDao: RefundDao,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) {
    fun getAllRefunds(): Flow<List<Refund>> = refundDao.getAllRefunds()

    suspend fun requestRefund(refund: Refund) {
        refundDao.insertRefund(refund)
    }

    suspend fun processRefund(refundId: String, approvedBy: String) {
        database.withTransaction {
            val refund = refundDao.getRefundById(refundId) ?: return@withTransaction
            if (refund.status != WorkflowStatus.PENDING) return@withTransaction

            // 1. Update refund status
            refundDao.approveRefund(refundId, WorkflowStatus.COMPLETED, approvedBy)

            // 2. Update sale status
            saleDao.updateSaleStatus(refund.saleId, "REFUNDED")

            // 3. Handle restock
            if (refund.restockItems) {
                val saleWithItems = saleDao.getSaleWithItemsNow(refund.saleId)
                saleWithItems?.items?.forEach { item ->
                    productDao.updateStockQuantity(item.productId, item.quantity)
                    stockMovementDao.insertMovement(
                        StockMovement(
                            id = UUID.randomUUID().toString(),
                            productId = item.productId,
                            quantity = item.quantity,
                            type = StockMovementType.RETURN,
                            reason = "Refund for sale ${refund.saleId}",
                            createdBy = approvedBy,
                            referenceId = refund.saleId
                        )
                    )
                }
            }
        }
    }
}
