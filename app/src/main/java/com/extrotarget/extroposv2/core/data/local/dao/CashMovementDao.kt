package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.CashMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface CashMovementDao {
    @Query("SELECT * FROM cash_movements WHERE shiftId = :shiftId")
    fun getMovementsForShift(shiftId: Long): Flow<List<CashMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: CashMovement)

    @Delete
    suspend fun deleteMovement(movement: CashMovement)
}
