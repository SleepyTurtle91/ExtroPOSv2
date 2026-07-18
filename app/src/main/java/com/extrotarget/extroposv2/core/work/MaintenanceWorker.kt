package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.extrotarget.extroposv2.core.platform.DiagnosticsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class MaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val diagnosticsManager: DiagnosticsManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.i("Starting scheduled database maintenance...")
        
        return try {
            // 1. Check Integrity
            val isHealthy = diagnosticsManager.checkDatabaseIntegrity()
            if (!isHealthy) {
                Timber.e("Database integrity check FAILED during maintenance")
                // In future: trigger emergency backup or alert
            }

            // 2. Perform VACUUM and Optimize
            val result = diagnosticsManager.performMaintenance()
            
            if (result.isSuccess) {
                Timber.i("Database maintenance completed successfully")
                Result.success()
            } else {
                Timber.e(result.exceptionOrNull(), "Database maintenance failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during MaintenanceWorker execution")
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "periodic_maintenance_worker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<MaintenanceWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
