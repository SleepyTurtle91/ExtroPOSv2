package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: String, quantity: java.math.BigDecimal)

    @Insert
    suspend fun insertStockMovement(movement: com.extrotarget.extroposv2.core.data.model.inventory.StockMovement)

    @Transaction
    suspend fun completeSale(sale: Sale, items: List<SaleItem>) {
        insertSale(sale)
        insertSaleItems(items)
        
        // Update stock for each item and record movement
        items.forEach { item ->
            decreaseStock(item.productId, item.quantity)
            insertStockMovement(
                com.extrotarget.extroposv2.core.data.model.inventory.StockMovement(
                    id = java.util.UUID.randomUUID().toString(),
                    productId = item.productId,
                    quantity = item.quantity.negate(),
                    type = "SALE",
                    timestamp = sale.timestamp,
                    note = "Sale ${sale.id}"
                )
            )
        }
    }

    @Update
    suspend fun updateSale(sale: Sale)

    @Transaction
    @Query("SELECT * FROM sales WHERE tableId = :tableId AND status = 'PENDING'")
    fun getPendingSaleWithItemsForTable(tableId: String): Flow<SaleWithItems?>

    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleById(saleId: String): Sale?

    @Query("SELECT * FROM sales WHERE tableId = :tableId AND status = 'PENDING'")
    suspend fun getPendingSaleForTable(tableId: String): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsBySaleId(saleId: String): List<SaleItem>

    @Query("UPDATE sale_items SET status = :status WHERE id = :itemId")
    suspend fun updateItemStatus(itemId: String, status: String)

    @Query("UPDATE sale_items SET status = :status WHERE saleId = :saleId AND printerTag = :tag")
    suspend fun updateItemsStatusByTag(saleId: String, tag: String, status: String)

    @Transaction
    @Query("SELECT * FROM sales WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getSalesWithItemsInRange(start: Long, end: Long): Flow<List<SaleWithItems>>

    @Query("SELECT * FROM sales WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>>
}
