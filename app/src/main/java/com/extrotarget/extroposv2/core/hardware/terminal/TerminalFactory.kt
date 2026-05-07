package com.extrotarget.extroposv2.core.hardware.terminal

import android.content.Context

enum class TerminalType {
    SIMULATED,
    PAX,
    INGENICO,
    GHL_JSON
}

class TerminalFactory(private val context: Context) {
    fun create(type: TerminalType, address: String? = null, port: Int? = null): TerminalInterface {
        return when (type) {
            TerminalType.SIMULATED -> SimulatedTerminal()
            TerminalType.PAX -> PaxTerminal(address ?: "127.0.0.1", port ?: 10009)
            TerminalType.INGENICO -> IngenicoTerminal(address ?: "127.0.0.1", port ?: 10009)
            TerminalType.GHL_JSON -> GhlTerminal(address ?: "127.0.0.1", port ?: 7000)
        }
    }
}

/**
 * Real-world implementation for GHL/IPAY88 style terminals using JSON over TCP.
 * This protocol is standard for many Malaysian payment acquirers.
 */
private class GhlTerminal(val address: String, val port: Int) : TerminalInterface {
    private var socket: java.net.Socket? = null
    private val gson = com.google.gson.Gson()

    override suspend fun connect(): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            socket = java.net.Socket()
            socket?.connect(java.net.InetSocketAddress(address, port), 5000)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun disconnect() {
        socket?.close()
        socket = null
    }

    override suspend fun isConnected(): Boolean = socket?.isConnected == true

    override suspend fun processPayment(amount: java.math.BigDecimal, invoiceNumber: String): TerminalResponse = 
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (socket == null || !socket!!.isConnected) connect()
                
                val request = mapOf(
                    "type" to "SALE",
                    "amount" to amount.movePointRight(2).toLong(), // Convert to cents for GHL
                    "invoice" to invoiceNumber,
                    "currency" to "MYR"
                )
                
                val writer = socket!!.getOutputStream().bufferedWriter()
                writer.write(gson.toJson(request) + "\n")
                writer.flush()
                
                val reader = socket!!.getInputStream().bufferedReader()
                val responseJson = reader.readLine()
                
                val responseMap = gson.fromJson(responseJson, Map::class.java)
                val status = responseMap["status"] as? String
                
                if (status == "SUCCESS") {
                    TerminalResponse.Success(
                        transactionId = responseMap["tid"] as? String ?: "",
                        approvalCode = responseMap["approval"] as? String ?: "",
                        cardType = responseMap["card"] as? String ?: "CARD",
                        maskedPan = responseMap["pan"] as? String ?: "****",
                        batchNumber = responseMap["batch"] as? String ?: "000001",
                        rawResponse = responseJson
                    )
                } else {
                    TerminalResponse.Error(responseMap["message"] as? String ?: "Declined")
                }
            } catch (e: Exception) {
                TerminalResponse.Error("Terminal Connection Error: ${e.message}")
            }
        }

    override suspend fun processVoid(transactionId: String): TerminalResponse = TerminalResponse.Error("Not Implemented")
    override suspend fun processSettlement(): TerminalResponse = TerminalResponse.Error("Not Implemented")
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
