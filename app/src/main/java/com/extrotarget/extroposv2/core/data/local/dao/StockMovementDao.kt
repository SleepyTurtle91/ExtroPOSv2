package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: String): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovement)

    @Query("SELECT SUM(quantity) FROM stock_movements WHERE productId = :productId")
    suspend fun getCurrentStock(productId: String): java.math.BigDecimal?
}