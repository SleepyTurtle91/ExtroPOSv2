package com.extrotarget.extroposv2.core.data.repository

import com.extrotarget.extroposv2.core.data.local.dao.OfflineQueueDao
import com.extrotarget.extroposv2.core.network.OfflineQueue
import com.extrotarget.extroposv2.core.network.QueueStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val offlineQueueDao: OfflineQueueDao
) {
    private val gson = Gson()

    fun getPendingQueue(): Flow<List<OfflineQueue>> = offlineQueueDao.getPendingQueue()

    suspend fun enqueueAction(type: String, payload: Any) {
        val item = OfflineQueue(
            actionType = type,
            payloadJson = gson.toJson(payload)
        )
        offlineQueueDao.insertItem(item)
    }

    suspend fun getPendingItems(): List<OfflineQueue> = offlineQueueDao.getPendingQueueNow()

    suspend fun markCompleted(id: Long, serverId: String? = null) {
        offlineQueueDao.markCompleted(id, serverId)
    }

    suspend fun markFailed(id: Long, error: String?, isPermanent: Boolean = false) {
        val status = if (isPermanent) QueueStatus.FAILED else QueueStatus.PENDING
        offlineQueueDao.markFailed(id, status, error)
    }
}
