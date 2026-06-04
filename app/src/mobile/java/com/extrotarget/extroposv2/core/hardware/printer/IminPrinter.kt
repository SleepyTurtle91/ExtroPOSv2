package com.extrotarget.extroposv2.core.hardware.printer

import android.content.Context
import com.imin.printer.PrinterHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import timber.log.Timber

/**
 * Implementation of PrinterInterface for iMin Swift 2 built-in thermal printer.
 * Uses reflection to ensure build stability across different SDK versions.
 */
class IminPrinter @Inject constructor(
    @ApplicationContext private val context: Context
) : PrinterInterface {
    
    private val printerHelper = PrinterHelper.getInstance()

    override suspend fun connect(): Boolean {
        return true
    }

    override suspend fun disconnect() {
    }

    override suspend fun isConnected(): Boolean {
        return true
    }

    private fun invokeMethod(name: String, vararg args: Any?) {
        try {
            val method = printerHelper.javaClass.methods.find { it.name == name && it.parameterCount == args.size }
            method?.invoke(printerHelper, *args)
        } catch (e: Exception) {
            Timber.w("iMin SDK method $name not found or failed: ${e.message}")
        }
    }

    override suspend fun printReceipt(content: List<PrintCommand>, charWidth: Int): Boolean {
        Timber.d("iMin Printing started (Reflection Mode): ${content.size} commands")
        return try {
            invokeMethod("initPrinter")
            
            content.forEach { command ->
                when (command) {
                    is PrintCommand.Header -> {
                        invokeMethod("setAlignment", 1)
                        invokeMethod("setTextSize", 28)
                        invokeMethod("printText", command.content + "\n")
                    }
                    is PrintCommand.Text -> {
                        invokeMethod("setTextSize", 24)
                        invokeMethod("printText", command.content + "\n")
                    }
                    is PrintCommand.Divider -> {
                        invokeMethod("printText", "-".repeat(charWidth) + "\n")
                    }
                    is PrintCommand.Feed -> {
                        repeat(command.lines) { invokeMethod("printAndLineFeed") }
                    }
                    is PrintCommand.QRCode -> {
                        invokeMethod("printQrCode", command.content, 1)
                    }
                    else -> {}
                }
            }
            
            invokeMethod("printAndFeedPaper", 100)
            true
        } catch (e: Exception) {
            Timber.e(e, "iMin Reflection Print failed")
            false
        }
    }

    override suspend fun getStatus(): PrinterStatus {
        return PrinterStatus.READY
    }
}
