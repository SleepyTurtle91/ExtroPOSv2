package com.extrotarget.extroposv2.core.domain.commerce

import java.math.BigDecimal

interface PaymentProcessor {
    suspend fun processPayment(transaction: CommerceTransaction, amount: BigDecimal): PaymentResult
    suspend fun refundPayment(transactionId: String, amount: BigDecimal): PaymentResult
}

data class PaymentResult(
    val isSuccess: Boolean,
    val transactionId: String?,
    val errorMessage: String? = null,
    val paymentData: Map<String, String> = emptyMap()
)
