package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.network.SyncMessageType
import com.extrotarget.extroposv2.core.network.StockUpdateData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val stockMovementDao: com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao,
    private val syncServer: com.extrotarget.extroposv2.core.network.SyncServer,
    private val syncClient: com.extrotarget.extroposv2.core.network.SyncClient,
    private val settingsRepository: com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = 
        productDao.getProductsByCategory(categoryId)

    suspend fun getProductById(id: String): Product? = productDao.getProductById(id)

    suspend fun getProductByBarcode(barcode: String): Product? = 
        productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product) {
        val role = settingsRepository.terminalRole.first()
        if (role == com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.SLAVE && syncClient.isConnected()) {
            syncClient.sendRealtimeMessage(SyncMessageType.UPDATE_PRODUCT, product)
        } else {
            productDao.insertProduct(product)
        }
    }

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun updateProduct(product: Product) {
        val role = settingsRepository.terminalRole.first()
        if (role == com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.SLAVE && syncClient.isConnected()) {
            syncClient.sendRealtimeMessage(SyncMessageType.UPDATE_PRODUCT, product)
        } else {
            productDao.updateProduct(product)
            if (syncServer.isRunning()) {
                syncServer.broadcastUpdate(SyncMessageType.STOCK_UPDATE, StockUpdateData(
                    productId = product.id,
                    newQuantity = product.stockQuantity,
                    isAvailable = product.isAvailable
                ))
            }
        }
    }

    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()

    suspend fun adjustStock(productId: String, quantity: java.math.BigDecimal, type: String, note: String?) {
        val role = settingsRepository.terminalRole.first()
        if (role == com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.SLAVE && syncClient.isConnected()) {
            syncClient.sendRealtimeMessage(SyncMessageType.UPDATE_STOCK, mapOf("productId" to productId, "adjustment" to quantity))
        } else {
            productDao.updateStockQuantity(productId, quantity)
            stockMovementDao.insertMovement(
                com.extrotarget.extroposv2.core.data.model.inventory.StockMovement(
                    id = java.util.UUID.randomUUID().toString(),
                    productId = productId,
                    quantity = quantity,
                    type = type,
                    note = note
                )
            )
        }
    }

    suspend fun setStock(productId: String, quantity: java.math.BigDecimal, type: String, note: String?) {
        productDao.setStockQuantity(productId, quantity)
        stockMovementDao.insertMovement(
            com.extrotarget.extroposv2.core.data.model.inventory.StockMovement(
                id = java.util.UUID.randomUUID().toString(),
                productId = productId,
                quantity = quantity,
                type = "SET",
                note = note
            )
        )
    }

    fun getStockMovements(productId: String) = stockMovementDao.getMovementsForProduct(productId)
}