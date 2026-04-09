package com.extrotarget.extroposv2.core.data.repository.inventory

import androidx.room.withTransaction
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.StockMovementDao
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val stockMovementDao: StockMovementDao,
    private val productDao: ProductDao,
    private val db: AppDatabase
) {
    fun getMovementsForProduct(productId: String): Flow<List<StockMovement>> {
        return stockMovementDao.getMovementsForProduct(productId)
    }

    suspend fun adjustStock(productId: String, quantity: BigDecimal, type: String, note: String? = null) {
        db.withTransaction {
            val movement = StockMovement(
                id = UUID.randomUUID().toString(),
                productId = productId,
                quantity = quantity,
                type = type,
                note = note
            )
            stockMovementDao.insertMovement(movement)
            productDao.updateStockQuantity(productId, quantity)
        }
    }

    suspend fun setStock(productId: String, quantity: BigDecimal, type: String, note: String? = null) {
        db.withTransaction {
            val currentStock = getCurrentStock(productId)
            val diff = quantity.subtract(currentStock)
            val movement = StockMovement(
                id = UUID.randomUUID().toString(),
                productId = productId,
                quantity = diff,
                type = type,
                note = note
            )
            stockMovementDao.insertMovement(movement)
            productDao.setStockQuantity(productId, quantity)
        }
    }

    suspend fun getCurrentStock(productId: String): BigDecimal {
        return stockMovementDao.getCurrentStock(productId) ?: BigDecimal.ZERO
    }
}