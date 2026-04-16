package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class LhdnPollingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val lhdnRepository: LhdnRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Starting LHDN document status polling...")
        
        val pendingSubmissions = lhdnRepository.getPendingSubmissions()
        if (pendingSubmissions.isEmpty()) {
            return Result.success()
        }

        var allSucceeded = true
        pendingSubmissions.forEach { submission ->
            submission.uuid?.let { uuid ->
                val result = lhdnRepository.pollDocumentStatus(uuid)
                if (result.isFailure) {
                    allSucceeded = false
                    Timber.e("Failed to poll status for UUID: $uuid")
                }
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "LhdnPollingWorker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<LhdnPollingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
