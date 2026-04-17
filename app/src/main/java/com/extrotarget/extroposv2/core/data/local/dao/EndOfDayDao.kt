package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.extrotarget.extroposv2.core.data.model.EndOfDay
import com.extrotarget.extroposv2.core.data.model.Shift
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface EndOfDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndOfDay(eod: EndOfDay): Long

    @Query("SELECT * FROM end_of_days ORDER BY businessDate DESC")
    fun getAllEndOfDays(): Flow<List<EndOfDay>>

    @Query("SELECT * FROM shifts WHERE startTime >= :startTime AND endTime <= :endTime AND isClosed = 1")
    suspend fun getClosedShiftsInRange(startTime: Long, endTime: Long): List<Shift>

    @Query("SELECT * FROM end_of_days WHERE businessDate = :date LIMIT 1")
    suspend fun getEndOfDayByDate(date: Long): EndOfDay?
    
    @Query("SELECT SUM(totalAmount) FROM sales WHERE timestamp >= :startTime AND timestamp <= :endTime AND status = 'COMPLETED'")
    suspend fun getTotalSalesInRange(startTime: Long, endTime: Long): BigDecimal?
}
