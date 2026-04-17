package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.extrotarget.extroposv2.core.data.model.Shift
import com.extrotarget.extroposv2.core.data.model.ShiftAdjustment
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE isClosed = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveShift(): Flow<Shift?>

    @Query("SELECT * FROM shifts WHERE isClosed = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveShiftNow(): Shift?

    @Query("SELECT * FROM shifts ORDER BY startTime DESC")
    fun getAllShifts(): Flow<List<Shift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: Shift): Long

    @Update
    suspend fun updateShift(shift: Shift)

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: Long): Shift?

    @Insert
    suspend fun insertAdjustment(adjustment: ShiftAdjustment)

    @Query("SELECT * FROM shift_adjustments WHERE shiftId = :shiftId ORDER BY timestamp DESC")
    fun getAdjustmentsForShift(shiftId: Long): Flow<List<ShiftAdjustment>>

    @Query("UPDATE shifts SET cashIn = cashIn + :amount WHERE id = :shiftId")
    suspend fun updateCashIn(shiftId: Long, amount: java.math.BigDecimal)

    @Query("UPDATE shifts SET cashOut = cashOut + :amount WHERE id = :shiftId")
    suspend fun updateCashOut(shiftId: Long, amount: java.math.BigDecimal)

    @Query("UPDATE shifts SET totalCashSales = totalCashSales + :amount WHERE id = :shiftId")
    suspend fun updateCashSales(shiftId: Long, amount: java.math.BigDecimal)

    @Query("UPDATE shifts SET totalOtherSales = totalOtherSales + :amount WHERE id = :shiftId")
    suspend fun updateOtherSales(shiftId: Long, amount: java.math.BigDecimal)

    @Query("UPDATE shifts SET totalTax = totalTax + :amount WHERE id = :shiftId")
    suspend fun updateTax(shiftId: Long, amount: java.math.BigDecimal)

    @Query("UPDATE shifts SET totalRounding = totalRounding + :amount WHERE id = :shiftId")
    suspend fun updateRounding(shiftId: Long, amount: java.math.BigDecimal)
}
