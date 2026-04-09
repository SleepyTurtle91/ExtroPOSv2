package com.extrotarget.extroposv2.core.data.local.dao.carwash

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CarWashDao {
    @Query("SELECT * FROM car_wash_jobs ORDER BY startTime DESC")
    fun getAllJobs(): Flow<List<CarWashJob>>

    @Query("SELECT * FROM car_wash_jobs WHERE status = :status")
    fun getJobsByStatus(status: CarWashStatus): Flow<List<CarWashJob>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: CarWashJob)

    @Update
    suspend fun updateJob(job: CarWashJob)

    @Query("UPDATE car_wash_jobs SET status = :status, completionTime = :completionTime WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: String, status: CarWashStatus, completionTime: Long?)

    @Query("UPDATE car_wash_jobs SET assignedStaffId = :staffId, assignedStaffName = :staffName, status = :status WHERE id = :jobId")
    suspend fun assignStaff(jobId: String, staffId: String, staffName: String, status: CarWashStatus)

    @Delete
    suspend fun deleteJob(job: CarWashJob)
}