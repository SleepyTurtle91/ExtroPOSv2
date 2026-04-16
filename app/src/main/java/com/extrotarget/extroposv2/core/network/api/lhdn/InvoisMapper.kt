package com.extrotarget.extroposv2.core.network.api.lhdn

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.lhdn.BuyerInfo
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.util.lhdn.LhdnInvoicingUtils
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Maps POS data to LHDN MyInvois JSON Schema (V1.0)
 * Reference: https://sdk.myinvois.lhdn.gov.my/docs/billing-pipeline/
 */
object InvoisMapper {

    fun mapToDocument(
        sale: Sale,
        items: List<SaleItem>,
        config: LhdnConfig,
        buyer: BuyerInfo = BuyerInfo(),
        isConsolidated: Boolean = false
    ): Map<String, Any> {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // LHDN requires Z (UTC) or specific offset. Using UTC for sandbox simplicity.
        val sdfTime = SimpleDateFormat("HH:mm:ss'Z'", Locale.getDefault())
        val now = Date(sale.timestamp)

        val receiver = if (isConsolidated) {
            mapOf(
                "tin" to "EI00000000010",
                "idn" to "NA",
                "idType" to "NRIC",
                "name" to "Consolidated Buyer",
                "address" to mapOf(
                    "line0" to "NA",
                    "city" to "NA",
                    "postalCode" to "00000",
                    "country" to "MYS"
                )
            )
        } else {
            mapOf(
                "tin" to (buyer.tin ?: "EI00000000010"),
                "idn" to (buyer.idValue ?: "NA"),
                "idType" to (buyer.idType ?: "NRIC"),
                "name" to (buyer.name ?: "General Public"),
                "address" to mapOf(
                    "line0" to (buyer.address ?: "NA"),
                    "city" to "NA",
                    "postalCode" to "00000",
                    "country" to "MYS"
                ),
                "contactNumber" to (buyer.contact ?: "0000000000")
            )
        }

        return mapOf(
            "issuer" to mapOf(
                "tin" to config.sellerTin,
                "idn" to config.sellerBrn,
                "idType" to "BRN",
                "name" to config.businessActivityDesc.ifBlank { "EXTRO TARGET SDN BHD" }, 
                "address" to mapOf(
                    "line0" to (config.sellerSstId ?: "No 1, Jalan Teknologi"),
                    "city" to "Petaling Jaya",
                    "postalCode" to "47810",
                    "country" to "MYS",
                    "state" to "10" // Selangor
                ),
                "contactNumber" to "03-12345678",
                "email" to "support@extrotarget.com"
            ),
            "receiver" to receiver,
            "dateTimeIssued" to sdfDate.format(now) + "T" + sdfTime.format(now),
            "documentType" to if (isConsolidated) "11" else "01", // 11 = Consolidated Invoice
            "documentVersion" to "1.1",
            "internalId" to LhdnInvoicingUtils.generateInternalId(sale, config.sellerSstId ?: "NA"),
            "documentItems" to items.map { item ->
                mapOf(
                    "description" to item.productName,
                    "quantity" to item.quantity.toDouble(),
                    "unitPrice" to item.unitPrice.toDouble(),
                    "taxType" to "01", // 01 = SST
                    "taxRate" to item.taxRate.toDouble(),
                    "taxAmount" to item.taxAmount.toDouble(),
                    "taxCategory" to "S", // S = Standard Rated
                    "taxExemptionReason" to if (item.taxRate.toDouble() == 0.0) "Zero-rated" else null,
                    "subtotal" to item.totalAmount.toDouble(),
                    "classification" to "022" // 022 = Retail
                )
            },
            "totalExcludingTax" to (sale.totalAmount - sale.taxAmount).toDouble(),
            "totalTaxAmount" to sale.taxAmount.toDouble(),
            "totalPayableAmount" to sale.totalAmount.toDouble(),
            "totalDiscountAmount" to sale.discountAmount.toDouble(),
            "totalRoundingAmount" to sale.roundingAdjustment.toDouble(),
            "netAmount" to sale.totalAmount.toDouble(),
            "invoiceCurrencyCode" to "MYR",
            "billingReference" to listOf(
                mapOf(
                    "id" to sale.id,
                    "description" to "POS Reference"
                )
            )
        )
    }
}
