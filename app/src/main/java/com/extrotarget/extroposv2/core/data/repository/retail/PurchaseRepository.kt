package com.extrotarget.extroposv2.core.data.repository.retail

import androidx.room.withTransaction
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.PurchaseOrderDao
import com.extrotarget.extroposv2.core.data.local.dao.SupplierDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrder
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrderItem
import com.extrotarget.extroposv2.domain.retail.model.Supplier
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import com.extrotarget.extroposv2.core.domain.commerce.StockMovement
import com.extrotarget.extroposv2.core.domain.commerce.StockMovementType
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val database: AppDatabase,
    private val poDao: PurchaseOrderDao,
    private val supplierDao: SupplierDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) {
    fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    suspend fun saveSupplier(supplier: Supplier) = supplierDao.insertSupplier(supplier)

    fun getAllPurchaseOrders(): Flow<List<PurchaseOrder>> = poDao.getAllPurchaseOrders()
    fun getPurchaseOrderWithItems(id: String) = poDao.getPurchaseOrderWithItems(id)

    suspend fun createPurchaseOrder(po: PurchaseOrder, items: List<PurchaseOrderItem>) {
        database.withTransaction {
            poDao.insertPurchaseOrder(po)
            items.forEach { poDao.insertPurchaseOrderItem(it) }
        }
    }

    suspend fun receivePurchaseOrder(poId: String, receivedItems: List<Pair<String, BigDecimal>>, staffId: String) {
        database.withTransaction {
            val po = poDao.getPurchaseOrderById(poId) ?: return@withTransaction
            val poItems = poDao.getItemsForPoNow(poId)

            receivedItems.forEach { (itemId, qty) ->
                if (qty <= BigDecimal.ZERO) return@forEach
                
                val item = poItems.find { it.id == itemId } ?: return@forEach

                // 1. Update receivedQuantity in PurchaseOrderItem
                poDao.updateReceivedQuantity(itemId, qty)

                // 2. Update stock in Product
                productDao.updateStockQuantity(item.productId, qty)

                // 3. Record StockMovement
                stockMovementDao.insertMovement(
                    StockMovement(
                        id = UUID.randomUUID().toString(),
                        productId = item.productId,
                        quantity = qty,
                        type = StockMovementType.RESTOCK,
                        reason = "Received PO ${po.poNumber}",
                        createdBy = staffId,
                        referenceId = poId
                    )
                )
            }

            // Re-fetch items to check total status
            val updatedItems = poDao.getItemsForPoNow(poId)
            val allReceived = updatedItems.all { it.receivedQuantity >= it.quantity }
            
            val newStatus = if (allReceived) WorkflowStatus.RECEIVED else WorkflowStatus.PARTIALLY_RECEIVED
            
            poDao.updatePurchaseOrder(po.copy(
                status = newStatus,
                receivedAt = if (allReceived) System.currentTimeMillis() else po.receivedAt,
                receivedBy = if (allReceived) staffId else po.receivedBy
            ))
        }
    }

    suspend fun updateStatus(poId: String, status: WorkflowStatus) {
        poDao.updatePurchaseOrderStatus(poId, status)
    }
}
