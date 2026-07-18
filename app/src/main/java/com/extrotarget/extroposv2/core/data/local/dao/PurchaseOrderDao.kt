package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrder
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrderItem
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseOrderDao {
    @Query("SELECT * FROM purchase_orders ORDER BY createdAt DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getPurchaseOrderById(id: String): PurchaseOrder?

    @Transaction
    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    fun getPurchaseOrderWithItems(id: String): Flow<PurchaseOrderWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(po: PurchaseOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrderItem(item: PurchaseOrderItem)

    @Update
    suspend fun updatePurchaseOrder(po: PurchaseOrder)

    @Query("UPDATE purchase_orders SET status = :status WHERE id = :id")
    suspend fun updatePurchaseOrderStatus(id: String, status: WorkflowStatus)

    @Query("UPDATE purchase_order_items SET receivedQuantity = receivedQuantity + :adjustment WHERE id = :itemId")
    suspend fun updateReceivedQuantity(itemId: String, adjustment: java.math.BigDecimal)

    @Query("SELECT * FROM purchase_order_items WHERE poId = :poId")
    suspend fun getItemsForPoNow(poId: String): List<PurchaseOrderItem>

    @Delete
    suspend fun deletePurchaseOrder(po: PurchaseOrder)

    @Query("DELETE FROM purchase_order_items WHERE poId = :poId")
    suspend fun deleteItemsForPo(poId: String)
}

data class PurchaseOrderWithItems(
    @Embedded val purchaseOrder: PurchaseOrder,
    @Relation(
        parentColumn = "id",
        entityColumn = "poId"
    )
    val items: List<PurchaseOrderItem>
)
