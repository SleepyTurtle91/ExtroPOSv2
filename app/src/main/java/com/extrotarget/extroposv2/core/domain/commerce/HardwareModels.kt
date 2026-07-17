package com.extrotarget.extroposv2.core.domain.commerce

import com.extrotarget.extroposv2.core.hardware.printer.PrintCommand
import com.extrotarget.extroposv2.core.hardware.printer.PrinterStatus

/**
 * Shared abstraction for any printing device (Receipt, Kitchen, Label).
 */
interface PrintDevice {
    val id: String
    val name: String
    val type: PrinterType
    
    suspend fun print(document: PrintDocument): Boolean
    suspend fun getStatus(): PrinterStatus
}

data class PrintDocument(
    val title: String,
    val commands: List<PrintCommand>,
    val copies: Int = 1
)

enum class PrinterType {
    RECEIPT, KITCHEN, LABEL, INVOICE
}
