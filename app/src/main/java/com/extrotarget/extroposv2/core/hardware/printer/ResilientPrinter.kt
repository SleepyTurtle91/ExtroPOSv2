package com.extrotarget.extroposv2.core.hardware.printer

import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * A decorator for [PrinterInterface] that adds auto-reconnect and retry logic
 * with exponential backoff and buffering.
 */
class ResilientPrinter(
    private val basePrinter: PrinterInterface,
    private val printBuffer: PrintBuffer? = null,
    private val printerId: String? = null
) : PrinterInterface {

    override suspend fun connect(): Boolean {
        var attempt = 0
        var delayMs = 1000L
        val maxAttempts = 3

        while (attempt < maxAttempts) {
            if (basePrinter.connect()) {
                Timber.d("ResilientPrinter: Connected successfully on attempt ${attempt + 1}")
                return true
            }
            attempt++
            if (attempt < maxAttempts) {
                Timber.w("ResilientPrinter: Connection attempt $attempt failed, retrying in ${delayMs}ms...")
                delay(delayMs)
                delayMs *= 2 // Exponential backoff
            }
        }
        Timber.e("ResilientPrinter: Failed to connect after $maxAttempts attempts")
        return false
    }

    override suspend fun disconnect() {
        basePrinter.disconnect()
    }

    override suspend fun isConnected(): Boolean {
        return basePrinter.isConnected()
    }

    override suspend fun printReceipt(content: List<PrintCommand>, charWidth: Int): Boolean {
        if (!isConnected()) {
            Timber.i("ResilientPrinter: Not connected, attempting auto-reconnect before printing...")
            if (!connect()) {
                return false
            }
        }

        var attempt = 0
        val maxAttempts = 2

        while (attempt < maxAttempts) {
            if (basePrinter.printReceipt(content, charWidth)) {
                return true
            }
            attempt++
            if (attempt < maxAttempts) {
                Timber.w("ResilientPrinter: Print failed, attempt $attempt, trying to reconnect and retry...")
                disconnect()
                if (connect()) {
                    delay(500) // Small breather after reconnect
                } else {
                    break // Reconnect failed, exit retry loop
                }
            }
        }

        // If we reach here, all retries failed. Buffer the job if possible.
        if (printBuffer != null && printerId != null) {
            Timber.i("ResilientPrinter: All retries failed, buffering job for printer $printerId")
            printBuffer.bufferJob(printerId, content, charWidth)
            return true // We return true because the job is now safely in the buffer to be retried later
        }

        return false
    }

    override suspend fun getStatus(): PrinterStatus {
        return basePrinter.getStatus()
    }
}
