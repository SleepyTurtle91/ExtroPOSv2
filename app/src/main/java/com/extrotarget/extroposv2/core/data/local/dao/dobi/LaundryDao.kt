package com.extrotarget.extroposv2.core.data.local.dao.dobi

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LaundryDao {
    @Query("SELECT * FROM laundry_orders ORDER BY receivedTimestamp DESC")
    fun getAllOrders(): Flow<List<LaundryOrder>>

    @Query("SELECT * FROM laundry_orders WHERE status = :status")
    fun getOrdersByStatus(status: LaundryStatus): Flow<List<LaundryOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: LaundryOrder)

    @Update
    suspend fun updateOrder(order: LaundryOrder)

    @Query("UPDATE laundry_orders SET status = :status, readyTimestamp = :timestamp WHERE id = :orderId")
    suspend fun markAsReady(orderId: String, status: LaundryStatus, timestamp: Long)

    @Query("UPDATE laundry_orders SET status = :status, collectedTimestamp = :timestamp WHERE id = :orderId")
    suspend fun markAsCollected(orderId: String, status: LaundryStatus, timestamp: Long)

    @Delete
    suspend fun deleteOrder(order: LaundryOrder)
}