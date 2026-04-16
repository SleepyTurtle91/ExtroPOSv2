package com.extrotarget.extroposv2.core.domain.usecase

import android.content.Context
import android.hardware.usb.UsbManager
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.hardware.printer.*
import com.extrotarget.extroposv2.core.util.printer.ReceiptGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class PrintReceiptUseCase @Inject constructor(
    private val printerDao: PrinterDao,
    private val receiptDao: ReceiptDao,
    private val lhdnRepository: LhdnRepository,
    private val taxRepository: com.extrotarget.extroposv2.core.data.repository.settings.TaxRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(sale: Sale, items: List<SaleItem>, tableName: String? = null) {
        val receiptConfig = receiptDao.getReceiptConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig()
        val taxConfig = taxRepository.getTaxConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.TaxConfig()
        val lhdnConfig = lhdnRepository.getConfig().firstOrNull()
        
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val defaultPrinter = allPrinters.find { it.isDefault } ?: allPrinters.firstOrNull()

        // 1. Print Main Receipt
        val lhdnSubmission = lhdnRepository.getSubmissionFlow(sale.id).firstOrNull()
        defaultPrinter?.let { config ->
            printToPrinter(config) {
                ReceiptGenerator.generateSaleReceipt(
                    sale = sale,
                    items = items,
                    config = receiptConfig,
                    taxConfig = taxConfig,
                    lhdnSubmission = lhdnSubmission,
                    isSandbox = lhdnConfig?.isSandbox ?: true
                )
            }
        }

        // 2. Print Order Slips
        val itemsByTag = items.groupBy { it.printerTag }
        allPrinters.filter { it.printerTag != null && it.printerTag != "RECEIPT" }.forEach { printerConfig ->
            val itemsForThisPrinter = itemsByTag[printerConfig.printerTag] ?: emptyList()
            if (itemsForThisPrinter.isNotEmpty()) {
                printToPrinter(printerConfig) {
                    ReceiptGenerator.generateOrderSlip(
                        saleId = sale.id,
                        tableName = tableName,
                        items = itemsForThisPrinter,
                        tag = printerConfig.printerTag ?: "ORDER"
                    )
                }
            }
        }
    }

    suspend fun printOrderSlip(saleId: String, tableName: String?, items: List<SaleItem>) {
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val itemsByTag = items.groupBy { it.printerTag }
        
        allPrinters.filter { it.printerTag != null && it.printerTag != "RECEIPT" }.forEach { printerConfig ->
            val itemsForThisPrinter = itemsByTag[printerConfig.printerTag] ?: emptyList()
            if (itemsForThisPrinter.isNotEmpty()) {
                printToPrinter(printerConfig) {
                    ReceiptGenerator.generateOrderSlip(
                        saleId = saleId,
                        tableName = tableName,
                        items = itemsForThisPrinter,
                        tag = printerConfig.printerTag ?: "ORDER"
                    )
                }
            }
        }
    }

    private suspend fun printToPrinter(config: PrinterConfig, generateCommands: () -> List<PrintCommand>) {
        val printer: PrinterInterface? = when (config.connectionType) {
            "BLUETOOTH" -> BluetoothPrinter(config.address)
            "NETWORK" -> NetworkPrinter(config.address, config.port)
            "USB" -> {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                val device = usbManager.deviceList.values.find { it.deviceName == config.address }
                device?.let { UsbPrinter(context, it) }
            }
            else -> null
        }

        printer?.let {
            if (it.connect()) {
                val commands = generateCommands()
                if (config.id == "default_printer" || config.printerTag == "RECEIPT") {
                    it.printReceipt(listOf(PrintCommand.DrawerKick) + commands)
                } else {
                    it.printReceipt(commands)
                }
                it.disconnect()
            }
        }
    }
}
