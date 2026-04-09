package com.extrotarget.extroposv2.core.data.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipt_configs")
data class ReceiptConfig(
    @PrimaryKey val id: String = "default_receipt",
    val storeName: String = "ExtroPOS Store",
    val address: String? = null,
    val phone: String? = null,
    val brn: String? = null, // Business Registration Number
    val sstId: String? = null, // Malaysian SST ID
    val headerMessage: String? = null,
    val footerMessage: String? = "Thank you! Please come again.",
    val showLogo: Boolean = false,
    val logoPath: String? = null,
    val showTaxSummary: Boolean = true,
    val showRounding: Boolean = true,
    val showLhdnQr: Boolean = true
)