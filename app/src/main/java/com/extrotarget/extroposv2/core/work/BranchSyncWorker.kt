package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.repository.SyncRepository
import com.extrotarget.extroposv2.core.network.BranchSyncManager
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class BranchSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val branchSyncManager: BranchSyncManager
) : CoroutineWorker(context, params) {

    private val gson = Gson()

    override suspend fun doWork(): Result {
        val pendingItems = syncRepository.getPendingItems()
        if (pendingItems.isEmpty()) return Result.success()

        var hasFailures = false

        for (item in pendingItems) {
            try {
                val syncResult: kotlin.Result<Unit> = when (item.actionType) {
                    "SALE" -> {
                        val saleWithItems = gson.fromJson(item.payloadJson, SaleWithItems::class.java)
                        branchSyncManager.pushSaleToHQ(saleWithItems)
                    }
                    else -> kotlin.Result.failure(Exception("Unknown action type: ${item.actionType}"))
                }

                if (syncResult.isSuccess) {
                    syncRepository.markCompleted(item.id)
                } else {
                    val error = syncResult.exceptionOrNull()?.message
                    Timber.e("Sync failed for item ${item.id}: $error")
                    syncRepository.markFailed(item.id, error)
                    hasFailures = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception processing sync item ${item.id}")
                syncRepository.markFailed(item.id, e.message)
                hasFailures = true
            }
        }

        return if (hasFailures) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    companion object {
        private const val WORK_NAME = "branch_sync_worker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<BranchSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
