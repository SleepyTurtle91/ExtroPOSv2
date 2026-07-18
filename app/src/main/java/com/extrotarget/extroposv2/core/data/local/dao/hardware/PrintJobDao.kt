package com.extrotarget.extroposv2.core.data.local.dao.hardware

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.hardware.PrintJob
import com.extrotarget.extroposv2.core.data.model.hardware.PrintJobStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintJobDao {
    @Query("SELECT * FROM print_jobs WHERE status = :status ORDER BY timestamp ASC")
    fun getJobsByStatus(status: PrintJobStatus): Flow<List<PrintJob>>

    @Query("SELECT * FROM print_jobs WHERE id = :id")
    suspend fun getJobById(id: String): PrintJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: PrintJob)

    @Update
    suspend fun updateJob(job: PrintJob)

    @Query("DELETE FROM print_jobs WHERE status = 'COMPLETED' AND timestamp < :beforeTimestamp")
    suspend fun cleanUpOldJobs(beforeTimestamp: Long)

    @Query("UPDATE print_jobs SET status = 'PENDING' WHERE status = 'FAILED'")
    suspend fun retryAllFailed()

    @Query("SELECT * FROM print_jobs WHERE status = 'FAILED' OR status = 'PENDING'")
    fun getActiveJobs(): Flow<List<PrintJob>>
}
