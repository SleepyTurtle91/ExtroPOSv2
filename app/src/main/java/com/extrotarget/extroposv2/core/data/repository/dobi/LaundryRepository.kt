package com.extrotarget.extroposv2.core.data.repository.dobi

import com.extrotarget.extroposv2.core.data.local.dao.dobi.LaundryDao
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaundryRepository @Inject constructor(
    private val laundryDao: LaundryDao
) {
    val allOrders: Flow<List<LaundryOrder>> = laundryDao.getAllOrders()

    fun getOrdersByStatus(status: LaundryStatus): Flow<List<LaundryOrder>> = 
        laundryDao.getOrdersByStatus(status)

    suspend fun createOrder(order: LaundryOrder) = laundryDao.insertOrder(order)

    suspend fun updateOrder(order: LaundryOrder) = laundryDao.updateOrder(order)

    suspend fun markAsReady(orderId: String) {
        laundryDao.markAsReady(orderId, LaundryStatus.READY, System.currentTimeMillis())
    }

    suspend fun markAsCollected(orderId: String) {
        laundryDao.markAsCollected(orderId, LaundryStatus.COLLECTED, System.currentTimeMillis())
    }
}