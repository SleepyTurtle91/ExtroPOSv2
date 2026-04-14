package com.extrotarget.extroposv2.core.domain.usecase

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.CarWashRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.TableRepository
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.data.repository.settings.AutoCountRepository
import com.extrotarget.extroposv2.core.data.model.lhdn.BuyerInfo
import com.extrotarget.extroposv2.core.work.AutoCountSyncWorker
import com.extrotarget.extroposv2.core.work.EInvoiceSubmissionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class ProcessSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val staffRepository: StaffRepository,
    private val carWashRepository: CarWashRepository,
    private val tableRepository: TableRepository,
    private val lhdnRepository: LhdnRepository,
    private val autoCountRepository: AutoCountRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun invoke(
        sale: Sale,
        saleItems: List<SaleItem>,
        commissionRecords: List<CommissionRecord>,
        selectedTableId: String? = null,
        buyerInfo: BuyerInfo? = null
    ) {
        // 1. Save Sale and Items
        saleRepository.completeSale(sale, saleItems)

        // 2. Save Commissions
        if (commissionRecords.isNotEmpty()) {
            staffRepository.addCommissionRecords(commissionRecords)
        }

        // 3. Trigger LHDN e-Invoice Submission if configured
        val lhdnConfig = lhdnRepository.getConfig().firstOrNull()
        if (lhdnConfig != null && lhdnConfig.isEnabled) {
            // Determine if it should be consolidated or individual
            // Rule: If buyerInfo is provided and has a name/TIN other than default, it's individual
            val isConsolidated = buyerInfo == null || (buyerInfo.tin == "EI00000000010" && buyerInfo.name == "General Public")
            enqueueEInvoiceSubmission(sale.id, isConsolidated)
        }

        // 4. Car Wash: Create jobs for any car wash items
        saleItems.filter { it.assignedStaffId != null }.forEach { item ->
            val plateNumber = item.modifiers?.split(", ")?.find { it.startsWith("Plate:") }?.removePrefix("Plate:") ?: "WALK-IN"
            
            carWashRepository.createJob(CarWashJob(
                id = UUID.randomUUID().toString(),
                plateNumber = plateNumber,
                serviceName = item.productName,
                price = item.totalAmount,
                assignedStaffId = item.assignedStaffId,
                assignedStaffName = item.assignedStaffName,
                status = CarWashStatus.QUEUED
            ))
        }

        // 5. F&B: Release Table if associated
        selectedTableId?.let { tableId ->
            tableRepository.releaseTable(tableId)
        }

        // 6. AutoCount Sync: If enabled
        val autoCountConfig = autoCountRepository.getConfig().firstOrNull()
        if (autoCountConfig != null && autoCountConfig.isEnabled && autoCountConfig.syncToken != null) {
            enqueueAutoCountSync(sale.id, autoCountConfig.syncToken)
        }
    }

    private fun enqueueAutoCountSync(saleId: String, token: String) {
        val workRequest = OneTimeWorkRequestBuilder<AutoCountSyncWorker>()
            .setInputData(workDataOf(
                AutoCountSyncWorker.KEY_SALE_ID to saleId,
                AutoCountSyncWorker.KEY_TOKEN to token
            ))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun enqueueEInvoiceSubmission(saleId: String, isConsolidated: Boolean = false) {
        val workRequest = OneTimeWorkRequestBuilder<EInvoiceSubmissionWorker>()
            .setInputData(workDataOf(
                EInvoiceSubmissionWorker.KEY_SALE_ID to saleId,
                EInvoiceSubmissionWorker.KEY_IS_CONSOLIDATED to isConsolidated
            ))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
