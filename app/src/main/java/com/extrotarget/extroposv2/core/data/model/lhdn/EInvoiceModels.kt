package com.extrotarget.extroposv2.core.data.model.lhdn

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.extrotarget.extroposv2.core.data.model.Sale
import java.math.BigDecimal

/**
 * Represents a sale link to an e-invoice submission
 */
@Entity(tableName = "sale_einvoice_submission")
data class SaleEInvoiceSubmission(
    @PrimaryKey val saleId: String,
    val submissionId: String? = null,
    val uuid: String? = null,
    val status: EInvoiceStatus = EInvoiceStatus.NOT_SUBMITTED,
    val lastResponse: String? = null,
    val lastAttemptTimestamp: Long = 0L,
    val lhdnValidationMessage: String? = null,
    val digitalSignature: String? = null
)

/**
 * LHDN MyInvois Requirement: Buyer Information
 * Required for individual e-Invoices (B2B/High value B2C)
 */
data class BuyerInfo(
    val name: String? = "General Public",
    val tin: String? = "EI00000000010", // Default for local buyers without TIN
    val idType: String? = "NRIC",        // NRIC, PASSPORT, BRN, etc.
    val idValue: String? = null,
    val address: String? = null,
    val contact: String? = null
)

/**
 * LHDN Document Status for MyInvois API
 */
enum class EInvoiceStatus {
    NOT_SUBMITTED,
    SUBMITTED,
    VALIDATED,
    REJECTED,
    CANCELLED
}

/**
 * Configuration for the Merchant (Seller) as per LHDN requirements
 */
@Entity(tableName = "lhdn_config")
data class LhdnConfig(
    @PrimaryKey val id: Int = 1,
    val sellerTin: String,
    val sellerBrn: String,
    val sellerSstId: String? = null,
    val msicCode: String, // Malaysia Standard Industrial Classification
    val businessActivityDesc: String,
    val clientId: String? = null,
    val clientSecret: String? = null,
    val isSandbox: Boolean = true,
    val isEnabled: Boolean = false
)

/**
 * Token storage for LHDN MyInvois API
 */
@Entity(tableName = "lhdn_tokens")
data class LhdnToken(
    @PrimaryKey val id: Int = 1,
    val accessToken: String,
    val expiryTimestamp: Long,
    val tokenType: String
)
