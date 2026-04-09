package com.extrotarget.extroposv2.core.hardware.terminal

import java.math.BigDecimal

interface TerminalInterface {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun isConnected(): Boolean
    
    suspend fun processPayment(amount: BigDecimal, invoiceNumber: String): TerminalResponse
    suspend fun processVoid(transactionId: String): TerminalResponse
    suspend fun processSettlement(): TerminalResponse
}

sealed class TerminalResponse {
    data class Success(
        val transactionId: String,
        val approvalCode: String,
        val cardType: String,
        val maskedPan: String,
        val batchNumber: String,
        val rawResponse: String? = null
    ) : TerminalResponse()
    
    data class Error(val message: String, val code: String? = null) : TerminalResponse()
    object Cancelled : TerminalResponse()
}
