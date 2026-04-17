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
import com.extrotarget.extroposv2.core.data.repository.ShiftRepository
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.CarWashRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.TableRepository
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.data.repository.settings.AutoCountRepository
import com.extrotarget.extroposv2.core.data.repository.loyalty.LoyaltyRepository
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
    private val loyaltyRepository: LoyaltyRepository,
    private val shiftRepository: ShiftRepository,
    @ApplicationContext private val context: Context
) {
    operator suspend fun invoke(
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
            // Rule 1: If buyerInfo is provided and has a name/TIN other than default, it's individual
            // Rule 2: (Malaysian 2026 Mandate) If total amount >= threshold, it MUST be individual
            val isHighValue = sale.totalAmount >= lhdnConfig.einvoiceThresholdAmount
            val isConsolidated = !isHighValue && (buyerInfo == null || (buyerInfo.tin == "EI00000000010" && buyerInfo.name == "General Public"))
            
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

        // 7. Loyalty Points: If a member is associated and loyalty is enabled
        sale.memberId?.let { memberId ->
            val loyaltyConfig = loyaltyRepository.getConfig().firstOrNull()
            if (loyaltyConfig != null && loyaltyConfig.isEnabled) {
                val member = loyaltyRepository.getMemberById(memberId)
                if (member != null) {
                    val multiplier = when (member.tier) {
                        "SILVER" -> loyaltyConfig.silverMultiplier
                        "GOLD" -> loyaltyConfig.goldMultiplier
                        else -> BigDecimal.ONE
                    }
                    val pointsEarned = sale.totalAmount.multiply(loyaltyConfig.pointsPerCurrencyUnit).multiply(multiplier)
                    loyaltyRepository.addPoints(memberId, pointsEarned, sale.id, "Earned from sale ${sale.id}")
                }
            }
        }

        // 8. Update Shift Totals
        if (sale.status == "COMPLETED") {
            shiftRepository.getActiveShiftNow()?.let { activeShift ->
                val cashAmount = if (sale.paymentMethod == "CASH") sale.totalAmount else BigDecimal.ZERO
                val otherAmount = if (sale.paymentMethod != "CASH") sale.totalAmount else BigDecimal.ZERO
                
                shiftRepository.recordSale(
                    shiftId = activeShift.id,
                    cashAmount = cashAmount,
                    otherAmount = otherAmount,
                    taxAmount = sale.taxAmount,
                    rounding = sale.roundingAdjustment
                )
            }
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
