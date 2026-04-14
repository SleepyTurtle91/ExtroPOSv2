package com.extrotarget.extroposv2.core.network.api.autocount

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoCountSyncManager @Inject constructor(
    private val api: AutoCountApi
) {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun mapToAutoCountCashSale(sale: Sale, items: List<SaleItem>): AutoCountCashSale {
        return AutoCountCashSale(
            DocNo = sale.id,
            DocDate = dateFormatter.format(Date(sale.timestamp)),
            CashAccountCode = if (sale.paymentMethod == "CASH") "300-000" else "300-001", // Example mapping
            TaxAmt = sale.taxAmount,
            TotalAmt = sale.totalAmount,
            RoundingAmt = sale.roundingAdjustment,
            Description = "POS Sale ${sale.id}",
            CashSaleDetails = items.map { item ->
                AutoCountCashSaleDetail(
                    ItemCode = item.productId,
                    Description = item.productName,
                    Qty = item.quantity,
                    UnitPrice = item.unitPrice,
                    TaxCode = "SR-S", // Standard Malaysian Tax Code for AutoCount
                    TaxAmt = item.taxAmount,
                    SubTotal = item.totalAmount,
                    Discount = item.discountAmount
                )
            }
        )
    }

    suspend fun syncSale(token: String, sale: Sale, items: List<SaleItem>): AutoCountSyncResponse {
        return try {
            val autoCountSale = mapToAutoCountCashSale(sale, items)
            val response = api.saveCashSale("Bearer $token", autoCountSale)
            if (response.isSuccessful) {
                response.body() ?: AutoCountSyncResponse(false, "Empty response body")
            } else {
                AutoCountSyncResponse(false, "API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            AutoCountSyncResponse(false, "Exception: ${e.localizedMessage}")
        }
    }
}
