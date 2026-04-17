package com.extrotarget.extroposv2.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Calendar

@HiltWorker
class LhdnConsolidationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val saleDao: SaleDao,
    private val lhdnRepository: LhdnRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("Starting LHDN Consolidation Worker")
        
        // 1. Determine the period to consolidate (e.g., yesterday)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        // 2. Fetch sales that haven't been submitted individually
        val salesWithItems = saleDao.getSalesWithItemsWithoutLhdnSubmission(startTime, endTime)

        if (salesWithItems.isEmpty()) {
            Timber.i("No sales to consolidate for period ${startTime}")
            return Result.success()
        }

        // 3. Submit to LHDN
        val result = lhdnRepository.submitConsolidatedEInvoice(
            sales = salesWithItems.map { it.sale },
            salesWithItems = salesWithItems,
            businessDate = startTime
        )

        return if (result.isSuccess) {
            Timber.i("Consolidated E-Invoice submitted: ${result.getOrNull()}")
            Result.success()
        } else {
            Timber.e(result.exceptionOrNull(), "Consolidated E-Invoice failed")
            Result.retry()
        }
    }
}
