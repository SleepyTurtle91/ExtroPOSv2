package com.extrotarget.extroposv2.core.data.repository.inventory

import com.extrotarget.extroposv2.core.data.local.dao.StockTransferDao
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransfer
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransferStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockTransferRepository @Inject constructor(
    private val stockTransferDao: StockTransferDao
) {
    fun getAllTransfers(): Flow<List<StockTransfer>> = stockTransferDao.getAllTransfers()

    fun getPendingTransfers(): Flow<List<StockTransfer>> = 
        stockTransferDao.getTransfersByStatus(StockTransferStatus.PENDING)

    suspend fun createTransfer(transfer: StockTransfer) = 
        stockTransferDao.insertTransfer(transfer)

    suspend fun updateTransfer(transfer: StockTransfer) = 
        stockTransferDao.updateTransfer(transfer)

    suspend fun getTransferById(id: String): StockTransfer? = 
        stockTransferDao.getTransferById(id)
}
