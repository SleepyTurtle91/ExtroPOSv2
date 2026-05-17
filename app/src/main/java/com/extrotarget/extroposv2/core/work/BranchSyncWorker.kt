package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.network.BranchSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class BranchSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val saleRepository: SaleRepository,
    private val branchSyncManager: BranchSyncManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val saleId = inputData.getString(KEY_SALE_ID) ?: return Result.failure()
        
        val sale = saleRepository.getSaleById(saleId) ?: return Result.failure()
        val items = saleRepository.getItemsBySaleId(saleId)
        val saleWithItems = SaleWithItems(sale, items)

        return try {
            val result = branchSyncManager.pushSaleToHQ(saleWithItems)
            if (result.isSuccess) {
                Result.success()
            } else {
                Timber.e("Branch Sync Failed: ${result.exceptionOrNull()?.message}")
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing sale to HQ")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_SALE_ID = "sale_id"
    }
}
