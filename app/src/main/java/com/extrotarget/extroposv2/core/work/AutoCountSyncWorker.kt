package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.network.api.autocount.AutoCountSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class AutoCountSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val saleRepository: SaleRepository,
    private val autoCountSyncManager: AutoCountSyncManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val saleId = inputData.getString(KEY_SALE_ID) ?: return Result.failure()
        val token = inputData.getString(KEY_TOKEN) ?: return Result.failure()

        val sale = saleRepository.getSaleById(saleId) ?: return Result.failure()
        if (sale.autoCountSyncStatus == "SYNCED") return Result.success()

        val items = saleRepository.getItemsBySaleId(saleId)

        return try {
            val response = autoCountSyncManager.syncSale(token, sale, items)
            if (response.Success) {
                val updatedSale = sale.copy(
                    autoCountSyncStatus = "SYNCED",
                    autoCountDocNo = response.DocNo
                )
                saleRepository.updateSale(updatedSale)
                Result.success()
            } else {
                Timber.e("AutoCount Sync Failed: ${response.Message}")
                val updatedSale = sale.copy(autoCountSyncStatus = "FAILED")
                saleRepository.updateSale(updatedSale)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing to AutoCount")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_SALE_ID = "sale_id"
        const val KEY_TOKEN = "token"
    }
}
