package com.extrotarget.extroposv2.core.data.repository.hardware

import android.content.Context
import com.extrotarget.extroposv2.core.hardware.terminal.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val factory = TerminalFactory(context)
    
    // In a real app, this would be fetched from database settings
    private var currentTerminalType = TerminalType.SIMULATED
    private var terminalAddress: String? = "127.0.0.1"
    private var terminalPort: Int? = 10009

    suspend fun processPayment(amount: BigDecimal, invoiceNumber: String): TerminalResponse {
        val terminal = factory.create(currentTerminalType, terminalAddress, terminalPort)
        if (terminal.connect()) {
            val response = terminal.processPayment(amount, invoiceNumber)
            terminal.disconnect()
            return response
        }
        return TerminalResponse.Error("Terminal Connection Failed")
    }

    suspend fun processVoid(transactionId: String): TerminalResponse {
        val terminal = factory.create(currentTerminalType, terminalAddress, terminalPort)
        if (terminal.connect()) {
            val response = terminal.processVoid(transactionId)
            terminal.disconnect()
            return response
        }
        return TerminalResponse.Error("Terminal Connection Failed")
    }
}
