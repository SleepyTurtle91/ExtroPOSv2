package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransfer
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransferStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransferDao {
    @Query("SELECT * FROM stock_transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<StockTransfer>>

    @Query("SELECT * FROM stock_transfers WHERE status = :status ORDER BY timestamp DESC")
    fun getTransfersByStatus(status: StockTransferStatus): Flow<List<StockTransfer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: StockTransfer)

    @Update
    suspend fun updateTransfer(transfer: StockTransfer)

    @Query("SELECT * FROM stock_transfers WHERE id = :id")
    suspend fun getTransferById(id: String): StockTransfer?
}
