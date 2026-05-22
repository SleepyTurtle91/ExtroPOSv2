package com.extrotarget.extroposv2.core.hardware.printer

import android.content.Context
import com.imin.printer.PrinterHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Implementation of PrinterInterface for iMin Swift 2 built-in thermal printer.
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

    override suspend fun printReceipt(content: List<PrintCommand>, charWidth: Int): Boolean {
        return try {
            printerHelper.initPrinter()
            
            content.forEach { command ->
                when (command) {
                    is PrintCommand.Header -> {
                        printerHelper.setAlignment(1) // Center
                        printerHelper.setTextStyle(1) // Bold
                        printerHelper.setTextSize(30)
                        printerHelper.printText(command.content + "\n")
                        printerHelper.setTextStyle(0) // Reset
                    }
                    is PrintCommand.BigText -> {
                        val align = when (command.alignment) {
                            Alignment.LEFT -> 0
                            Alignment.CENTER -> 1
                            Alignment.RIGHT -> 2
                        }
                        printerHelper.setAlignment(align)
                        printerHelper.setTextSize(28)
                        printerHelper.setTextStyle(1)
                        printerHelper.printText(command.content + "\n")
                        printerHelper.setTextStyle(0)
                    }
                    is PrintCommand.Text -> {
                        val align = when (command.alignment) {
                            Alignment.LEFT -> 0
                            Alignment.CENTER -> 1
                            Alignment.RIGHT -> 2
                        }
                        printerHelper.setAlignment(align)
                        printerHelper.setTextSize(24)
                        printerHelper.setTextStyle(if (command.isBold) 1 else 0)
                        printerHelper.printText(command.content + "\n")
                        printerHelper.setTextStyle(0)
                    }
                    is PrintCommand.Image -> {
                        val align = when (command.alignment) {
                            Alignment.LEFT -> 0
                            Alignment.CENTER -> 1
                            Alignment.RIGHT -> 2
                        }
                        printerHelper.setAlignment(align)
                        printerHelper.printBitmap(command.bitmap)
                    }
                    is PrintCommand.Divider -> {
                        printerHelper.setAlignment(1)
                        printerHelper.printText("-".repeat(charWidth) + "\n")
                    }
                    is PrintCommand.Buzzer -> {
                        // iMin doesn't have a direct buzzer command in some helper versions, 
                        // but usually it's handled by system or specific SDK call
                    }
                    is PrintCommand.Feed -> {
                        printerHelper.printAndLineFeed()
                        if (command.lines > 1) {
                            repeat(command.lines - 1) { printerHelper.printAndLineFeed() }
                        }
                    }
                    is PrintCommand.Cut -> {
                        // Handled by device or auto-cut
                    }
                    is PrintCommand.DrawerKick -> {
                        // iMin Swift 2 doesn't have drawer port usually
                    }
                    is PrintCommand.QRCode -> {
                        printerHelper.setAlignment(1)
                        printerHelper.printQrCode(command.content, 1) // 1 = Model 2
                    }
                    is PrintCommand.Raw -> {
                        // printerHelper.sendRawData(command.bytes)
                    }
                }
            }
            
            printerHelper.printAndFeedPaper(100)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getStatus(): PrinterStatus {
        return PrinterStatus.READY
    }
}
