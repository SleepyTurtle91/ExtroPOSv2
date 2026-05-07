package com.extrotarget.extroposv2.core.domain.usecase

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.Shift
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
    private val printerFactory: PrinterFactory,
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
            printToPrinter(config, receiptConfig.paperWidth.charWidth) {
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
                printToPrinter(printerConfig, receiptConfig.paperWidth.charWidth) {
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
        val receiptConfig = receiptDao.getReceiptConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig()
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val itemsByTag = items.groupBy { it.printerTag }
        
        allPrinters.filter { it.printerTag != null && it.printerTag != "RECEIPT" }.forEach { printerConfig ->
            val itemsForThisPrinter = itemsByTag[printerConfig.printerTag] ?: emptyList()
            if (itemsForThisPrinter.isNotEmpty()) {
                printToPrinter(printerConfig, receiptConfig.paperWidth.charWidth) {
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

    suspend fun printZReport(shift: Shift) {
        val receiptConfig = receiptDao.getReceiptConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig()
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val defaultPrinter = allPrinters.find { it.isDefault } ?: allPrinters.firstOrNull()

        defaultPrinter?.let { config ->
            printToPrinter(config, receiptConfig.paperWidth.charWidth) {
                ReceiptGenerator.generateZReport(shift, receiptConfig)
            }
        }
    }

    suspend fun printEodReport(eod: com.extrotarget.extroposv2.core.data.model.EndOfDay) {
        val receiptConfig = receiptDao.getReceiptConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig()
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val defaultPrinter = allPrinters.find { it.isDefault } ?: allPrinters.firstOrNull()

        defaultPrinter?.let { config ->
            printToPrinter(config, receiptConfig.paperWidth.charWidth) {
                ReceiptGenerator.generateEodReport(eod, receiptConfig)
            }
        }
    }

    suspend fun openCashDrawer() {
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val drawerPrinter = allPrinters.find { it.printerTag == "RECEIPT" } 
            ?: allPrinters.find { it.isDefault } 
            ?: allPrinters.firstOrNull()

        drawerPrinter?.let { config ->
            printToPrinter(config) {
                listOf(PrintCommand.DrawerKick)
            }
        }
    }

    suspend fun printTableQr(tableName: String, qrContent: String) {
        val receiptConfig = receiptDao.getReceiptConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig()
        val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
        val defaultPrinter = allPrinters.find { it.isDefault } ?: allPrinters.firstOrNull()

        defaultPrinter?.let { config ->
            printToPrinter(config, receiptConfig.paperWidth.charWidth) {
                ReceiptGenerator.generateTableQrSticker(tableName, qrContent)
            }
        }
    }

    private suspend fun printToPrinter(
        config: PrinterConfig, 
        charWidth: Int = 32, 
        generateCommands: () -> List<PrintCommand>
    ) {
        val printer = printerFactory.create(config)

        printer?.let {
            if (it.connect()) {
                val commands = generateCommands()
                val finalCommands = if (config.printerTag == "RECEIPT" || config.isDefault) {
                    listOf(PrintCommand.DrawerKick) + commands
                } else {
                    commands
                }
                it.printReceipt(finalCommands, charWidth)
                it.disconnect()
            }
        }
    }
}
