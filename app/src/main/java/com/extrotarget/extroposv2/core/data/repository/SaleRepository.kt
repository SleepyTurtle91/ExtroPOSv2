package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import com.extrotarget.extroposv2.core.network.SyncServer
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val syncServer: SyncServer
) {
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>> =
        saleDao.getAllSalesWithItems()

    suspend fun completeSale(sale: Sale, items: List<SaleItem>) {
        saleDao.completeSale(sale, items)
        if (syncServer.isRunning()) {
            syncServer.broadcastUpdate("SALE_COMPLETED", SaleWithItems(sale, items))
        }
    }

    suspend fun getSaleById(saleId: String): Sale? = saleDao.getSaleById(saleId)

    suspend fun getPendingSaleForTable(tableId: String): Sale? = 
        saleDao.getPendingSaleForTable(tableId)

    fun getPendingSaleWithItemsForTable(tableId: String): Flow<SaleWithItems?> =
        saleDao.getPendingSaleWithItemsForTable(tableId)

    suspend fun updateSale(sale: Sale) = saleDao.updateSale(sale)

    suspend fun getItemsBySaleId(saleId: String): List<SaleItem> = 
        saleDao.getItemsBySaleId(saleId)

    suspend fun updateItemStatus(itemId: String, status: String) =
        saleDao.updateItemStatus(itemId, status)

    suspend fun updateItemsStatusByTag(saleId: String, tag: String, status: String) =
        saleDao.updateItemsStatusByTag(saleId, tag, status)

    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>> =
        saleDao.getSalesInRange(start, end)

    suspend fun updateLocalStock(productId: String, quantity: java.math.BigDecimal) {
        val product = productDao.getProductById(productId)
        productDao.setStockQuantity(productId, quantity)
        
        // Check for low stock alert
        if (product != null && quantity <= product.minStockLevel && product.minStockLevel > java.math.BigDecimal.ZERO) {
            _stockAlerts.emit(product.name)
        }
    }

    private val _stockAlerts = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val stockAlerts = _stockAlerts.asSharedFlow()
}
