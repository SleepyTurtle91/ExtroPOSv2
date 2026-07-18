package com.extrotarget.extroposv2.core.hardware.printer

import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.model.hardware.PrintJobStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterManager @Inject constructor(
    private val printerDao: PrinterDao,
    private val printerFactory: PrinterFactory,
    private val printBuffer: PrintBuffer,
    private val gson: Gson
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isProcessing = false

    fun startBufferProcessor() {
        scope.launch {
            while (isActive) {
                processPendingJobs()
                delay(30000) // Check every 30 seconds
            }
        }
    }

    suspend fun processPendingJobs() {
        if (isProcessing) return
        isProcessing = true
        
        try {
            val pendingJobs = printBuffer.getActiveJobs().firstOrNull()?.filter { it.status == PrintJobStatus.PENDING }
            if (pendingJobs.isNullOrEmpty()) return

            Timber.d("Processing ${pendingJobs.size} pending print jobs")

            pendingJobs.forEach { job ->
                val config = printerDao.getConfigById(job.printerId)
                if (config != null) {
                    val printer = printerFactory.create(config)
                    if (printer != null) {
                        try {
                            val content: List<PrintCommand> = gson.fromJson(
                                job.contentJson,
                                object : TypeToken<List<PrintCommand>>() {}.type
                            )
                            
                            // Important: when printing from buffer, we should use a direct printer 
                            // to avoid re-buffering if it fails again (though ResilientPrinter handles it).
                            // Actually, let's use the base printer if possible to avoid infinite loops.
                            
                            if (printer.connect()) {
                                if (printer.printReceipt(content, job.charWidth)) {
                                    printBuffer.updateJobStatus(job.id, PrintJobStatus.COMPLETED)
                                } else {
                                    printBuffer.incrementRetryCount(job.id, "Print failed")
                                }
                                printer.disconnect()
                            } else {
                                printBuffer.incrementRetryCount(job.id, "Connection failed")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error processing print job ${job.id}")
                            printBuffer.updateJobStatus(job.id, PrintJobStatus.FAILED, e.message)
                        }
                    } else {
                        printBuffer.updateJobStatus(job.id, PrintJobStatus.FAILED, "Printer not found")
                    }
                } else {
                    printBuffer.updateJobStatus(job.id, PrintJobStatus.FAILED, "Printer config missing")
                }
            }
        } finally {
            isProcessing = false
        }
    }

    suspend fun printNow(printerId: String, content: List<PrintCommand>, charWidth: Int): Boolean {
        val config = printerDao.getConfigById(printerId) ?: return false
        val printer = printerFactory.create(config) ?: return false
        
        return try {
            if (printer.connect()) {
                val result = printer.printReceipt(content, charWidth)
                printer.disconnect()
                result
            } else {
                // If direct print fails, ResilientPrinter (if used) will have buffered it.
                // Wait, printerFactory.create returns ResilientPrinter.
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error printing now")
            false
        }
    }
}
