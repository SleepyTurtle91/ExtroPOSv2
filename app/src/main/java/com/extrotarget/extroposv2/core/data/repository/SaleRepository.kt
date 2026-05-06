package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.network.SyncMessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.extrotarget.extroposv2.core.network.SyncServer
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: com.extrotarget.extroposv2.core.data.local.dao.SaleDao,
    private val productDao: com.extrotarget.extroposv2.core.data.local.dao.ProductDao,
    private val syncServer: com.extrotarget.extroposv2.core.network.SyncServer,
    private val syncClient: com.extrotarget.extroposv2.core.network.SyncClient,
    private val settingsRepository: com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
) {
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>> =
        saleDao.getAllSalesWithItems()

    suspend fun completeSale(sale: Sale, items: List<SaleItem>) {
        saleDao.completeSale(sale, items)
        
        // Broadcast to Slaves if we are Master
        if (syncServer.isRunning()) {
            syncServer.broadcastUpdate(SyncMessageType.SALE_COMPLETED, SaleWithItems(sale, items))
        }
        
        // Push to Master if we are Slave
        val role = settingsRepository.terminalRole.first()
        if (role == com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.SLAVE) {
            syncClient.sendRealtimeMessage(SyncMessageType.PUSH_SALE, SaleWithItems(sale, items))
        }
    }

    suspend fun getSaleById(saleId: String): Sale? = saleDao.getSaleById(saleId)

    suspend fun getPendingSaleForTable(tableId: String): Sale? = 
        saleDao.getPendingSaleForTable(tableId)

    fun getPendingSaleWithItemsForTable(tableId: String): Flow<SaleWithItems?> =
        saleDao.getPendingSaleWithItemsForTable(tableId)

    suspend fun updateSale(sale: Sale) = saleDao.updateSale(sale)

    suspend fun voidSale(saleId: String) {
        val sale = saleDao.getSaleById(saleId) ?: return
        if (sale.status == "VOIDED") return

        val items = saleDao.getItemsBySaleId(saleId)
        
        // Update sale status
        saleDao.updateSale(sale.copy(status = "VOIDED"))

        // Restore stock
        items.forEach { item ->
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                val newStock = product.stockQuantity.add(item.quantity)
                productDao.setStockQuantity(item.productId, newStock)
                
                // Record stock movement (RESTORE)
                saleDao.insertStockMovement(
                    com.extrotarget.extroposv2.core.data.model.inventory.StockMovement(
                        id = java.util.UUID.randomUUID().toString(),
                        productId = item.productId,
                        quantity = item.quantity,
                        type = "VOID_RESTORE",
                        timestamp = System.currentTimeMillis(),
                        note = "Voided Sale $saleId"
                    )
                )
            }
        }

        if (syncServer.isRunning()) {
            syncServer.broadcastUpdate(SyncMessageType.SALE_VOIDED, saleId)
        }
    }

    suspend fun getItemsBySaleId(saleId: String): List<SaleItem> = 
        saleDao.getItemsBySaleId(saleId)

    suspend fun updateItemStatus(itemId: String, status: String) =
        saleDao.updateItemStatus(itemId, status)

    suspend fun updateItemsStatusByTag(saleId: String, tag: String, status: String) =
        saleDao.updateItemsStatusByTag(saleId, tag, status)

    fun getSalesInRange(start: Long, end: Long): Flow<List<Sale>> =
        saleDao.getSalesInRange(start, end)

    suspend fun getSalesInRangeNow(start: Long, end: Long): List<Sale> =
        saleDao.getSalesInRangeNow(start, end)

    suspend fun updateLocalStock(productId: String, quantity: java.math.BigDecimal, isAvailable: Boolean? = null) {
        val product = productDao.getProductById(productId)
        productDao.setStockQuantity(productId, quantity)
        if (isAvailable != null) {
            product?.let {
                productDao.updateProduct(it.copy(isAvailable = isAvailable))
            }
        }
        
        // Check for low stock alert
        if (product != null && quantity <= product.minStockLevel && product.minStockLevel > java.math.BigDecimal.ZERO) {
            _stockAlerts.emit(product.name)
        }
    }

    private val _stockAlerts = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val stockAlerts = _stockAlerts.asSharedFlow()
}
