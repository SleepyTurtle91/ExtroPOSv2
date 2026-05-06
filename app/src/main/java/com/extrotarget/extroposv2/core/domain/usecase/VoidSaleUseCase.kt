package com.extrotarget.extroposv2.core.domain.usecase

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.work.EInvoiceSubmissionWorker
import com.extrotarget.extroposv2.core.util.audit.AuditManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class VoidSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val lhdnRepository: LhdnRepository,
    private val auditManager: AuditManager,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(saleId: String, reason: String) {
        // 1. Execute Void in Repository (Status + Stock)
        saleRepository.voidSale(saleId)

        // 2. Audit Log
        auditManager.logAction("VOID_SALE", "Voided Sale $saleId. Reason: $reason", "SALES", "WARNING")

        // 3. Trigger LHDN Credit Note if e-Invoicing is enabled
        val lhdnConfig = lhdnRepository.getConfig().firstOrNull()
        if (lhdnConfig != null && lhdnConfig.isEnabled) {
            enqueueCreditNoteSubmission(saleId)
        }
    }

    private fun enqueueCreditNoteSubmission(saleId: String) {
        val workRequest = OneTimeWorkRequestBuilder<EInvoiceSubmissionWorker>()
            .setInputData(workDataOf(
                EInvoiceSubmissionWorker.KEY_SALE_ID to saleId,
                "is_credit_note" to true // We'll update the worker to handle this
            ))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
