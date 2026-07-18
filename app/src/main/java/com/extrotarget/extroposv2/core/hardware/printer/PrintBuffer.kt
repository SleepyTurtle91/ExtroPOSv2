package com.extrotarget.extroposv2.core.hardware.printer

import com.extrotarget.extroposv2.core.data.local.dao.hardware.PrintJobDao
import com.extrotarget.extroposv2.core.data.model.hardware.PrintJob
import com.extrotarget.extroposv2.core.data.model.hardware.PrintJobStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrintBuffer @Inject constructor(
    private val printJobDao: PrintJobDao,
    private val gson: Gson
) {
    suspend fun bufferJob(printerId: String, content: List<PrintCommand>, charWidth: Int): String {
        val job = PrintJob(
            printerId = printerId,
            contentJson = gson.toJson(content),
            charWidth = charWidth,
            status = PrintJobStatus.PENDING
        )
        printJobDao.insertJob(job)
        Timber.d("Buffered print job: ${job.id} for printer: $printerId")
        return job.id
    }

    fun getActiveJobs(): Flow<List<PrintJob>> = printJobDao.getActiveJobs()

    suspend fun updateJobStatus(jobId: String, status: PrintJobStatus, error: String? = null) {
        val job = printJobDao.getJobById(jobId)
        if (job != null) {
            printJobDao.updateJob(job.copy(status = status, lastError = error))
            Timber.d("Updated print job $jobId status to $status")
        }
    }

    suspend fun incrementRetryCount(jobId: String, error: String?) {
        val job = printJobDao.getJobById(jobId)
        if (job != null) {
            printJobDao.updateJob(job.copy(
                retryCount = job.retryCount + 1,
                lastError = error,
                status = if (job.retryCount >= 3) PrintJobStatus.FAILED else PrintJobStatus.PENDING
            ))
        }
    }

    suspend fun retryAllFailed() {
        printJobDao.retryAllFailed()
    }
}
