package com.extrotarget.extroposv2.core.hardware.terminal

import android.content.Context

enum class TerminalType {
    SIMULATED,
    PAX,
    INGENICO
}

class TerminalFactory(private val context: Context) {
    fun create(type: TerminalType, address: String? = null, port: Int? = null): TerminalInterface {
        return when (type) {
            TerminalType.SIMULATED -> SimulatedTerminal()
            TerminalType.PAX -> PaxTerminal(address ?: "127.0.0.1", port ?: 10009)
            TerminalType.INGENICO -> IngenicoTerminal(address ?: "127.0.0.1", port ?: 10009)
        }
    }
}

// Internal mock for development
private class SimulatedTerminal : TerminalInterface {
    override suspend fun connect(): Boolean = true
    override suspend fun disconnect() {}
    override suspend fun isConnected(): Boolean = true
    
    override suspend fun processPayment(amount: java.math.BigDecimal, invoiceNumber: String): TerminalResponse {
        kotlinx.coroutines.delay(2000)
        return TerminalResponse.Success(
            transactionId = "SIM_${System.currentTimeMillis()}",
            approvalCode = "123456",
            cardType = "VISA",
            maskedPan = "4111********1111",
            batchNumber = "000001"
        )
    }
    
    override suspend fun processVoid(transactionId: String): TerminalResponse {
        return TerminalResponse.Success("VOID_${System.currentTimeMillis()}", "000000", "", "", "")
    }
    
    override suspend fun processSettlement(): TerminalResponse {
        return TerminalResponse.Success("SET_${System.currentTimeMillis()}", "000000", "", "", "")
    }
}

// Placeholders for actual implementations
private class PaxTerminal(val address: String, val port: Int) : TerminalInterface {
    override suspend fun connect(): Boolean = false
    override suspend fun disconnect() {}
    override suspend fun isConnected(): Boolean = false
    override suspend fun processPayment(amount: java.math.BigDecimal, invoiceNumber: String): TerminalResponse = TerminalResponse.Error("Not Implemented")
    override suspend fun processVoid(transactionId: String): TerminalResponse = TerminalResponse.Error("Not Implemented")
    override suspend fun processSettlement(): TerminalResponse = TerminalResponse.Error("Not Implemented")
}

private class IngenicoTerminal(val address: String, val port: Int) : TerminalInterface {
    override suspend fun connect(): Boolean = false
    override suspend fun disconnect() {}
    override suspend fun isConnected(): Boolean = false
    override suspend fun processPayment(amount: java.math.BigDecimal, invoiceNumber: String): TerminalResponse = TerminalResponse.Error("Not Implemented")
    override suspend fun processVoid(transactionId: String): TerminalResponse = TerminalResponse.Error("Not Implemented")
    override suspend fun processSettlement(): TerminalResponse = TerminalResponse.Error("Not Implemented")
}
