package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.network.OfflineQueue
import com.extrotarget.extroposv2.core.network.QueueStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineQueueDao {
    @Query("SELECT * FROM offline_queue WHERE status = :status ORDER BY createdAt ASC")
    fun getQueueByStatus(status: QueueStatus): Flow<List<OfflineQueue>>

    @Query("SELECT * FROM offline_queue WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    fun getPendingQueue(): Flow<List<OfflineQueue>>

    @Query("SELECT * FROM offline_queue WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    suspend fun getPendingQueueNow(): List<OfflineQueue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: OfflineQueue): Long

    @Update
    suspend fun updateItem(item: OfflineQueue)

    @Delete
    suspend fun deleteItem(item: OfflineQueue)

    @Query("UPDATE offline_queue SET status = :status, lastError = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun markFailed(id: Long, status: QueueStatus, error: String?)

    @Query("UPDATE offline_queue SET status = 'COMPLETED', serverReferenceId = :serverId WHERE id = :id")
    suspend fun markCompleted(id: Long, serverId: String?)
}
