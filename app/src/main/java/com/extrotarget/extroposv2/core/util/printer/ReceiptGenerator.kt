package com.extrotarget.extroposv2.core.util.printer

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.Shift
import com.extrotarget.extroposv2.core.data.model.EndOfDay
import com.extrotarget.extroposv2.core.data.model.lhdn.SaleEInvoiceSubmission
import com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig
import com.extrotarget.extroposv2.core.hardware.printer.Alignment
import com.extrotarget.extroposv2.core.hardware.printer.PrintCommand
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

object ReceiptGenerator {
    
    fun generateSaleReceipt(
        sale: Sale,
        items: List<SaleItem>,
        config: ReceiptConfig = ReceiptConfig(),
        taxConfig: com.extrotarget.extroposv2.core.data.model.settings.TaxConfig = com.extrotarget.extroposv2.core.data.model.settings.TaxConfig(),
        lhdnSubmission: SaleEInvoiceSubmission? = null,
        isSandbox: Boolean = true
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        // Header
        commands.add(PrintCommand.Header(config.storeName))
        config.address?.let { commands.add(PrintCommand.Text(it, Alignment.CENTER)) }
        config.phone?.let { commands.add(PrintCommand.Text("Tel: $it", Alignment.CENTER)) }
        
        val brnLine = config.brn?.let { "BRN: $it" } ?: ""
        val sstLine = config.sstId?.let { "Tax ID: $it" } ?: ""
        
        if (brnLine.isNotEmpty() || sstLine.isNotEmpty()) {
            val combined = listOfNotNull(config.brn?.let { "BRN: $it" }, config.sstId?.let { "Tax ID: $it" })
                .joinToString(" | ")
            commands.add(PrintCommand.Text(combined, Alignment.CENTER))
        }

        commands.add(PrintCommand.Divider)

        // Sale Info
        commands.add(PrintCommand.Text("Receipt ID: ${sale.id.takeLast(8)}"))
        commands.add(PrintCommand.Text("Date: ${sdf.format(Date(sale.timestamp))}"))
        commands.add(PrintCommand.Divider)

        val charWidth = config.paperWidth.charWidth

        // Items
        items.forEach { item ->
            commands.add(PrintCommand.Text(item.productName, isBold = true))
            val priceLine = "${CurrencyUtils.format(item.unitPrice)} x ${item.quantity.stripTrailingZeros().toPlainString()}"
            val totalLine = CurrencyUtils.format(item.totalAmount)
            
            // Format line to push total to the right
            val spaces = charWidth - priceLine.length - totalLine.length
            val line = if (spaces > 0) {
                priceLine + " ".repeat(spaces) + totalLine
            } else {
                "$priceLine $totalLine"
            }
            commands.add(PrintCommand.Text(line))

            if (!item.modifiers.isNullOrBlank()) {
                val mods = item.modifiers.split(", ")
                mods.forEach { mod ->
                    commands.add(PrintCommand.Text("  * $mod"))
                }
            }

            if (item.discountAmount > java.math.BigDecimal.ZERO) {
                val discLine = "  - Discount:"
                val discValue = "-${CurrencyUtils.format(item.discountAmount)}"
                val discSpaces = charWidth - discLine.length - discValue.length
                val formattedDisc = if (discSpaces > 0) discLine + " ".repeat(discSpaces) + discValue else "$discLine $discValue"
                commands.add(PrintCommand.Text(formattedDisc))
            }
        }
        commands.add(PrintCommand.Divider)

        // Totals
        fun formatTotalLine(label: String, value: java.math.BigDecimal, isBold: Boolean = false, isNegative: Boolean = false) {
            val valueStr = (if (isNegative) "-" else "") + CurrencyUtils.format(value)
            val spaces = charWidth - label.length - valueStr.length
            val line = if (spaces > 0) label + " ".repeat(spaces) + valueStr else "$label $valueStr"
            commands.add(PrintCommand.Text(line, alignment = Alignment.LEFT, isBold = isBold))
        }

        val subtotal = sale.subtotal
        formatTotalLine("Subtotal:", subtotal)

        if (sale.discountAmount > java.math.BigDecimal.ZERO) {
            val label = sale.discountLabel?.let { "Discount ($it):" } ?: "Discount:"
            formatTotalLine(label, sale.discountAmount, isNegative = true)
        }

        if (sale.serviceChargeAmount > java.math.BigDecimal.ZERO) {
            val scLabel = "Service Charge (${taxConfig.serviceChargeRate}%):"
            formatTotalLine(scLabel, sale.serviceChargeAmount)
        }

        if (sale.taxAmount > java.math.BigDecimal.ZERO) {
            val taxLabel = "${taxConfig.taxName} (${taxConfig.defaultTaxRate}%):"
            formatTotalLine(taxLabel, sale.taxAmount)
        }
        
        val rounding = sale.roundingAdjustment
        if (config.showRounding && rounding != java.math.BigDecimal.ZERO) {
            formatTotalLine("Rounding:", rounding)
        }

        commands.add(PrintCommand.Divider)
        formatTotalLine("TOTAL:", sale.totalAmount, isBold = true)
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text("Payment Method: ${sale.paymentMethod}", Alignment.CENTER))
        config.footerMessage?.let { commands.add(PrintCommand.Text(it, Alignment.CENTER)) }
        
        // LHDN e-Invoice QR Code
        if (config.showLhdnQr && lhdnSubmission?.uuid != null) {
            commands.add(PrintCommand.Feed(1))
            commands.add(PrintCommand.Text("LHDN E-INVOICE VERIFICATION", Alignment.CENTER, isBold = true))
            
            val baseUrl = if (isSandbox) "https://preprod.myinvois.hasil.gov.my" else "https://myinvois.hasil.gov.my"
            val lhdnUrl = "$baseUrl/viewer/uuid/${lhdnSubmission.uuid}"

            commands.add(PrintCommand.QRCode(lhdnUrl))
            commands.add(PrintCommand.Text("UUID: ${lhdnSubmission.uuid.take(8)}...", Alignment.CENTER))
            
            lhdnSubmission.digitalSignature?.let { signature ->
                commands.add(PrintCommand.Text("LHDN DIGITAL SIGNATURE", Alignment.CENTER, isBold = true))
                commands.add(PrintCommand.Text(signature.take(32), Alignment.CENTER))
                commands.add(PrintCommand.Text(signature.drop(32).take(32), Alignment.CENTER))
            }
        } else if (config.showLhdnQr) {
            commands.add(PrintCommand.Feed(1))
            commands.add(PrintCommand.QRCode("https://extropos.com/verify/${sale.id}"))
        }
        
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }

    fun generateZReport(
        shift: Shift,
        config: ReceiptConfig = ReceiptConfig()
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val charWidth = config.paperWidth.charWidth

        fun formatLine(label: String, value: String, isBold: Boolean = false) {
            val spaces = charWidth - label.length - value.length
            val line = if (spaces > 0) label + " ".repeat(spaces) + value else "$label $value"
            commands.add(PrintCommand.Text(line, isBold = isBold))
        }

        commands.add(PrintCommand.Header("Z-REPORT (SHIFT CLOSE)"))
        commands.add(PrintCommand.Text(config.storeName, Alignment.CENTER))
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text("Staff: ${shift.staffName}"))
        commands.add(PrintCommand.Text("Terminal: ${shift.terminalId}"))
        commands.add(PrintCommand.Text("Opened: ${sdf.format(Date(shift.startTime))}"))
        shift.endTime?.let { commands.add(PrintCommand.Text("Closed: ${sdf.format(Date(it))}")) }
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text("FINANCIAL SUMMARY", isBold = true))
        formatLine("Gross Sales:", CurrencyUtils.format(shift.totalCashSales.add(shift.totalOtherSales)))
        formatLine("Tax Collected:", CurrencyUtils.format(shift.totalTax))
        formatLine("Rounding:", CurrencyUtils.format(shift.totalRounding))
        
        val netTotal = shift.totalCashSales.add(shift.totalOtherSales).add(shift.totalTax).add(shift.totalRounding)
        formatLine("TOTAL REVENUE:", CurrencyUtils.format(netTotal), isBold = true)
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text("CASH RECONCILIATION", isBold = true))
        formatLine("Starting Float:", CurrencyUtils.format(shift.startFloat))
        formatLine("Cash Sales:", CurrencyUtils.format(shift.totalCashSales))
        formatLine("Cash In:", CurrencyUtils.format(shift.cashIn))
        formatLine("Cash Out:", "-${CurrencyUtils.format(shift.cashOut)}")
        
        commands.add(PrintCommand.Divider)
        
        val expected = shift.endExpectedCash ?: java.math.BigDecimal.ZERO
        formatLine("Expected Cash:", CurrencyUtils.format(expected))
        
        val actual = shift.endActualCash ?: java.math.BigDecimal.ZERO
        formatLine("Actual Cash:", CurrencyUtils.format(actual))
        
        val discrepancy = actual.subtract(expected)
        val discrepancyLabel = if (discrepancy < java.math.BigDecimal.ZERO) "SHORTAGE" else "OVERAGE"
        formatLine(discrepancyLabel + ":", CurrencyUtils.format(discrepancy.abs()), isBold = true)

        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(1))
        commands.add(PrintCommand.Text("SIGNATURE", Alignment.CENTER))
        commands.add(PrintCommand.Feed(2))
        commands.add(PrintCommand.Text("____________________", Alignment.CENTER))
        commands.add(PrintCommand.Text(shift.staffName, Alignment.CENTER))

        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }

    fun generateEodReport(
        eod: EndOfDay,
        config: ReceiptConfig = ReceiptConfig()
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val charWidth = config.paperWidth.charWidth

        fun formatLine(label: String, value: String, isBold: Boolean = false) {
            val spaces = charWidth - label.length - value.length
            val line = if (spaces > 0) label + " ".repeat(spaces) + value else "$label $value"
            commands.add(PrintCommand.Text(line, isBold = isBold))
        }

        commands.add(PrintCommand.Header("END OF DAY SUMMARY"))
        commands.add(PrintCommand.Text(config.storeName, Alignment.CENTER))
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text("Business Date: ${sdf.format(Date(eod.businessDate))}"))
        commands.add(PrintCommand.Text("Generated By: ${eod.staffName}"))
        commands.add(PrintCommand.Text("Shifts Closed: ${eod.shiftCount}"))
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text("SALES SUMMARY", isBold = true))
        formatLine("Gross Sales:", CurrencyUtils.format(eod.grossSales))
        formatLine("Total Discount:", "-${CurrencyUtils.format(eod.totalDiscount)}")
        formatLine("Total Service Charge:", CurrencyUtils.format(eod.totalServiceCharge))
        formatLine("Total Tax:", CurrencyUtils.format(eod.totalTax))
        formatLine("Rounding:", CurrencyUtils.format(eod.totalRounding))
        
        commands.add(PrintCommand.Divider)
        formatLine("NET SALES:", CurrencyUtils.format(eod.netSales), isBold = true)
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text("PAYMENT BREAKDOWN", isBold = true))
        formatLine("Cash:", CurrencyUtils.format(eod.totalCashSales))
        formatLine("Other (Card/QR):", CurrencyUtils.format(eod.totalOtherSales))

        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }

    fun generateOrderSlip(
        saleId: String,
        tableName: String?,
        items: List<SaleItem>,
        tag: String
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        
        commands.add(PrintCommand.Header("${tag} ORDER"))
        tableName?.let { 
            commands.add(PrintCommand.BigText("TABLE: $it", Alignment.CENTER)) 
        }
        commands.add(PrintCommand.BigText("Order: ${saleId.takeLast(6)}", Alignment.CENTER))
        commands.add(PrintCommand.Text("Time: ${sdf.format(Date())}", Alignment.CENTER))
        commands.add(PrintCommand.Divider)
        
        items.forEach { item ->
            // Use bold text for the item name and quantity
            commands.add(PrintCommand.Text("${item.quantity.stripTrailingZeros().toPlainString()}x ${item.productName.uppercase()}", isBold = true))
            
            // Print modifiers/remarks clearly
            if (!item.modifiers.isNullOrBlank()) {
                val mods = item.modifiers.split(", ")
                mods.forEach { mod ->
                    commands.add(PrintCommand.Text("  ** $mod", isBold = true))
                }
            }
            
            // Add a small gap between items
            commands.add(PrintCommand.Feed(1))
        }
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)
        
        return commands
    }

    fun generateTableQrSticker(tableName: String, qrContent: String): List<PrintCommand> {
        return listOf(
            PrintCommand.Header("SCAN TO ORDER"),
            PrintCommand.BigText(tableName, Alignment.CENTER),
            PrintCommand.Divider,
            PrintCommand.Feed(1),
            PrintCommand.QRCode(qrContent),
            PrintCommand.Feed(1),
            PrintCommand.Text("Branch: MAIN", Alignment.CENTER),
            PrintCommand.Text("Powered by ExtroPOS", Alignment.CENTER),
            PrintCommand.Feed(3),
            PrintCommand.Cut
        )
    }
}
