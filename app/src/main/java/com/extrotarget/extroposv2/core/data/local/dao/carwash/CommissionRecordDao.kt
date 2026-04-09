package com.extrotarget.extroposv2.core.data.local.dao.carwash

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionRecordDao {
    @Query("SELECT * FROM commission_records WHERE staffId = :staffId ORDER BY timestamp DESC")
    fun getCommissionRecordsForStaff(staffId: String): Flow<List<CommissionRecord>>

    @Query("SELECT * FROM commission_records WHERE saleId = :saleId")
    suspend fun getCommissionRecordsForSale(saleId: String): List<CommissionRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissionRecord(record: CommissionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissionRecords(records: List<CommissionRecord>)

    @Query("SELECT SUM(calculatedCommission) FROM commission_records WHERE staffId = :staffId")
    fun getTotalEarningsForStaff(staffId: String): Flow<java.math.BigDecimal?>
}