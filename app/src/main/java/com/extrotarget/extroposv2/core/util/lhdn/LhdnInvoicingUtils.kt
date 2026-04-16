package com.extrotarget.extroposv2.core.util.lhdn

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object LhdnInvoicingUtils {
    
    /**
     * Generates a unique Internal Id for LHDN submission.
     * Format: {SST_ID}-{TIMESTAMP}-{SALE_ID_HASH}
     */
    fun generateInternalId(sale: Sale, sellerSstId: String): String {
        val hash = sha256(sale.id).take(8).uppercase()
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date(sale.timestamp))
        return "$sellerSstId-$timestamp-$hash"
    }

    /**
     * For B2C Consolidated e-Invoices, LHDN requires grouping by tax rate.
     */
    fun groupItemsByTax(items: List<SaleItem>): Map<Double, List<SaleItem>> {
        return items.groupBy { it.taxRate.toDouble() }
    }

    /**
     * Calculates the SHA-256 hash required for LHDN digital signatures and document verification.
     */
    fun calculateDocumentHash(jsonString: String): String {
        return sha256(jsonString)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
