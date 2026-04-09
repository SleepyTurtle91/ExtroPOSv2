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

    @Transaction
    suspend fun completeSale(sale: Sale, items: List<SaleItem>) {
        insertSale(sale)
        insertSaleItems(items)
    }

    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleById(saleId: String): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsBySaleId(saleId: String): List<SaleItem>

    @Query("SELECT * FROM sales WHERE timestamp >= :start AND timestamp <= :end")
    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>>
}