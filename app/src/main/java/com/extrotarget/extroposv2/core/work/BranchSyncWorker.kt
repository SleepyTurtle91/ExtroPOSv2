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

/**
 * Worker to reliably push local sales to the HQ branch in the background.
 */
@HiltWorker
class BranchSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val saleRepository: SaleRepository,
    private val branchSyncManager: BranchSyncManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val saleId = inputData.getString(KEY_SALE_ID) ?: return Result.failure()

        return try {
            val sale = saleRepository.getSaleById(saleId) ?: return Result.failure()
            val items = saleRepository.getItemsBySaleId(saleId)
            
            // Check if already synced to HQ (assuming localSyncStatus is used for HQ sync)
            if (sale.localSyncStatus == "SYNCED") return Result.success()

            val result = branchSyncManager.pushSaleToHQ(SaleWithItems(sale, items))
            
            if (result.isSuccess) {
                saleRepository.markSaleAsSynced(saleId)
                Result.success()
            } else {
                Timber.e("Branch Sync Failed for sale $saleId: ${result.exceptionOrNull()?.message}")
                if (runAttemptCount < 5) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during Branch Sync for sale $saleId")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_SALE_ID = "sale_id"
    }
}
