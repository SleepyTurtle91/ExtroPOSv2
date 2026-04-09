package com.extrotarget.extroposv2.core.data.repository.carwash

import com.extrotarget.extroposv2.core.data.local.dao.carwash.CarWashDao
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarWashRepository @Inject constructor(
    private val carWashDao: CarWashDao
) {
    val allJobs: Flow<List<CarWashJob>> = carWashDao.getAllJobs()

    fun getJobsByStatus(status: CarWashStatus): Flow<List<CarWashJob>> = 
        carWashDao.getJobsByStatus(status)

    suspend fun createJob(job: CarWashJob) = carWashDao.insertJob(job)

    suspend fun updateJob(job: CarWashJob) = carWashDao.updateJob(job)

    suspend fun startJob(jobId: String, staffId: String, staffName: String) {
        carWashDao.assignStaff(jobId, staffId, staffName, CarWashStatus.IN_PROGRESS)
    }

    suspend fun completeJob(jobId: String) {
        carWashDao.updateJobStatus(jobId, CarWashStatus.COMPLETED, System.currentTimeMillis())
    }

    suspend fun cancelJob(jobId: String) {
        carWashDao.updateJobStatus(jobId, CarWashStatus.CANCELLED, null)
    }
}