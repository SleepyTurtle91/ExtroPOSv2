package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extrotarget.extroposv2.core.data.model.lhdn.EInvoiceStatus
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.data.model.lhdn.BuyerInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EInvoiceSubmissionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val lhdnRepository: LhdnRepository,
    private val saleRepository: SaleRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val saleId = inputData.getString(KEY_SALE_ID) ?: return Result.failure()
        val isConsolidated = inputData.getBoolean(KEY_IS_CONSOLIDATED, false)

        val sale = saleRepository.getSaleById(saleId) ?: return Result.failure()
        val items = saleRepository.getItemsBySaleId(saleId)
        
        val buyer = BuyerInfo() 

        val result = lhdnRepository.submitEInvoice(sale, items, buyer, isConsolidated)

        return if (result.isSuccess) {
            Result.success()
        } else {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_SALE_ID = "sale_id"
        const val KEY_IS_CONSOLIDATED = "is_consolidated"
    }
}
