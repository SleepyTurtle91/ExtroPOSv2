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
 * Maps POS data to LHDN MyInvois JSON Schema (Simplified for Sandbox validation)
 */
object InvoisMapper {

    fun mapToDocument(
        sale: Sale,
        items: List<SaleItem>,
        config: LhdnConfig,
        buyer: BuyerInfo = BuyerInfo()
    ): Map<String, Any> {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss'Z'", Locale.getDefault())
        val now = Date(sale.timestamp)

        return mapOf(
            "issuer" to mapOf(
                "tin" to config.sellerTin,
                "idn" to config.sellerBrn,
                "msic" to config.msicCode,
                "name" to "Your Merchant Name", // Should come from a Business Profile entity
                "address" to mapOf(
                    "line0" to "Merchant Address Line 1",
                    "city" to "Kuala Lumpur",
                    "postalCode" to "50000",
                    "country" to "MYS"
                )
            ),
            "receiver" to mapOf(
                "tin" to (buyer.tin ?: "EI00000000010"),
                "idn" to (buyer.idValue ?: "NA"),
                "idType" to (buyer.idType ?: "BRN"),
                "name" to (buyer.name ?: "General Public"),
                "address" to mapOf(
                    "line0" to (buyer.address ?: "NA"),
                    "city" to "NA",
                    "postalCode" to "00000",
                    "country" to "MYS"
                )
            ),
            "dateTimeIssued" to sdfDate.format(now) + "T" + sdfTime.format(now),
            "documentType" to "01", // Invoice
            "documentVersion" to "1.0",
            "internalId" to LhdnInvoicingUtils.generateInternalId(sale, config.sellerSstId ?: "NA"),
            "documentItems" to items.map { item ->
                mapOf(
                    "description" to item.productName,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitPrice,
                    "taxType" to "01", // Default to SST
                    "taxRate" to item.taxRate,
                    "taxAmount" to item.taxAmount,
                    "subtotal" to item.totalAmount
                )
            },
            "totalExcludingTax" to (sale.totalAmount - sale.taxAmount),
            "totalTaxAmount" to sale.taxAmount,
            "totalPayableAmount" to sale.totalAmount
        )
    }
}
