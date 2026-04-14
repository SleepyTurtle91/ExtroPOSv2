package com.extrotarget.extroposv2.core.network.api.autocount

import java.math.BigDecimal

data class AutoCountCashSale(
    val DocNo: String? = null,
    val DocDate: String, // Format: YYYY-MM-DD
    val DebtorCode: String = "CASH",
    val CashAccountCode: String, // e.g., 300-000 for Cash, 300-001 for Card
    val Description: String? = null,
    val TaxAmt: BigDecimal,
    val TotalAmt: BigDecimal,
    val RoundingAmt: BigDecimal = BigDecimal.ZERO,
    val CashSaleDetails: List<AutoCountCashSaleDetail>
)

data class AutoCountCashSaleDetail(
    val ItemCode: String,
    val Description: String,
    val Qty: BigDecimal,
    val UnitPrice: BigDecimal,
    val TaxCode: String? = null, // e.g., SR-S, ZRL
    val TaxAmt: BigDecimal,
    val SubTotal: BigDecimal,
    val Discount: BigDecimal = BigDecimal.ZERO
)

data class AutoCountAuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

data class AutoCountSyncResponse(
    val Success: Boolean,
    val Message: String? = null,
    val DocNo: String? = null
)
