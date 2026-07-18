package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.Refund
import com.extrotarget.extroposv2.core.domain.commerce.WorkflowStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RefundDao {
    @Query("SELECT * FROM refunds ORDER BY timestamp DESC")
    fun getAllRefunds(): Flow<List<Refund>>

    @Query("SELECT * FROM refunds WHERE id = :id")
    suspend fun getRefundById(id: String): Refund?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefund(refund: Refund)

    @Update
    suspend fun updateRefund(refund: Refund)

    @Query("UPDATE refunds SET status = :status, approvedBy = :approvedBy WHERE id = :id")
    suspend fun approveRefund(id: String, status: WorkflowStatus, approvedBy: String)

    @Query("SELECT * FROM refunds WHERE saleId = :saleId")
    suspend fun getRefundsBySaleId(saleId: String): List<Refund>
}
