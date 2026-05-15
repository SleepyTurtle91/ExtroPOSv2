package com.extrotarget.extroposv2.core.util.printer

import android.content.Context
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.config.AppConfig
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
        context: Context,
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
        config.phone?.let { commands.add(PrintCommand.Text(context.getString(R.string.receipt_tel, it), Alignment.CENTER)) }
        
        val brnLine = config.brn?.let { context.getString(R.string.receipt_brn, it) } ?: ""
        val sstLine = config.sstId?.let { context.getString(R.string.receipt_tax_id, it) } ?: ""
        
        if (brnLine.isNotEmpty() || sstLine.isNotEmpty()) {
            val combined = listOfNotNull(config.brn?.let { context.getString(R.string.receipt_brn, it) }, config.sstId?.let { context.getString(R.string.receipt_tax_id, it) })
                .joinToString(" | ")
            commands.add(PrintCommand.Text(combined, Alignment.CENTER))
        }

        commands.add(PrintCommand.Divider)

        // Sale Info
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_id, sale.id.takeLast(8))))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_date, sdf.format(Date(sale.timestamp)))))
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
                val discLine = "  - " + context.getString(R.string.receipt_discount)
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
        formatTotalLine(context.getString(R.string.receipt_subtotal), subtotal)

        if (sale.discountAmount > java.math.BigDecimal.ZERO) {
            val label = sale.discountLabel?.let { context.getString(R.string.receipt_discount_label, it) } ?: context.getString(R.string.receipt_discount)
            formatTotalLine(label, sale.discountAmount, isNegative = true)
        }

        if (sale.serviceChargeAmount > java.math.BigDecimal.ZERO) {
            val scLabel = context.getString(R.string.receipt_service_charge, taxConfig.serviceChargeRate.toString())
            formatTotalLine(scLabel, sale.serviceChargeAmount)
        }

        if (sale.taxAmount > java.math.BigDecimal.ZERO) {
            val taxLabel = context.getString(R.string.receipt_tax_label, taxConfig.taxName, taxConfig.defaultTaxRate.toString())
            formatTotalLine(taxLabel, sale.taxAmount)
        }
        
        val rounding = sale.roundingAdjustment
        if (config.showRounding && rounding != java.math.BigDecimal.ZERO) {
            formatTotalLine(context.getString(R.string.receipt_rounding), rounding)
        }

        commands.add(PrintCommand.Divider)
        formatTotalLine(context.getString(R.string.receipt_total), sale.totalAmount, isBold = true)
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_payment_method, sale.paymentMethod), Alignment.CENTER))
        config.footerMessage?.let { commands.add(PrintCommand.Text(it, Alignment.CENTER)) }
        
        // LHDN e-Invoice QR Code
        if (config.showLhdnQr && lhdnSubmission?.uuid != null) {
            commands.add(PrintCommand.Feed(1))
            commands.add(PrintCommand.Text(context.getString(R.string.receipt_lhdn_verification), Alignment.CENTER, isBold = true))
            
            val baseUrl = AppConfig.URLs.getLhdnViewerUrl(isSandbox)
            val lhdnUrl = "$baseUrl/viewer/uuid/${lhdnSubmission.uuid}"

            commands.add(PrintCommand.QRCode(lhdnUrl))
            commands.add(PrintCommand.Text(context.getString(R.string.receipt_uuid, lhdnSubmission.uuid.take(8)), Alignment.CENTER))
            
            lhdnSubmission.digitalSignature?.let { signature ->
                commands.add(PrintCommand.Text(context.getString(R.string.receipt_lhdn_signature), Alignment.CENTER, isBold = true))
                commands.add(PrintCommand.Text(signature.take(32), Alignment.CENTER))
                commands.add(PrintCommand.Text(signature.drop(32).take(32), Alignment.CENTER))
            }
        } else if (config.showLhdnQr) {
            commands.add(PrintCommand.Feed(1))
            commands.add(PrintCommand.QRCode("${AppConfig.URLs.VERIFY_SALE_BASE}${sale.id}"))
        }
        
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }

    fun generateZReport(
        context: Context,
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

        commands.add(PrintCommand.Header(context.getString(R.string.receipt_z_report)))
        commands.add(PrintCommand.Text(config.storeName, Alignment.CENTER))
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text(context.getString(R.string.receipt_staff, shift.staffName)))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_terminal, shift.terminalId)))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_opened, sdf.format(Date(shift.startTime)))))
        shift.endTime?.let { commands.add(PrintCommand.Text(context.getString(R.string.receipt_closed, sdf.format(Date(it))))) }
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text(context.getString(R.string.receipt_financial_summary), isBold = true))
        formatLine(context.getString(R.string.receipt_gross_sales), CurrencyUtils.format(shift.totalCashSales.add(shift.totalOtherSales)))
        formatLine(context.getString(R.string.receipt_tax_collected), CurrencyUtils.format(shift.totalTax))
        formatLine(context.getString(R.string.receipt_rounding), CurrencyUtils.format(shift.totalRounding))
        
        val netTotal = shift.totalCashSales.add(shift.totalOtherSales).add(shift.totalTax).add(shift.totalRounding)
        formatLine(context.getString(R.string.receipt_total_revenue), CurrencyUtils.format(netTotal), isBold = true)
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_cash_recon), isBold = true))
        formatLine(context.getString(R.string.receipt_start_float), CurrencyUtils.format(shift.startFloat))
        formatLine(context.getString(R.string.receipt_cash_sales), CurrencyUtils.format(shift.totalCashSales))
        formatLine(context.getString(R.string.receipt_cash_in), CurrencyUtils.format(shift.cashIn))
        formatLine(context.getString(R.string.receipt_cash_out), "-${CurrencyUtils.format(shift.cashOut)}")
        
        commands.add(PrintCommand.Divider)
        
        val expected = shift.endExpectedCash ?: java.math.BigDecimal.ZERO
        formatLine(context.getString(R.string.receipt_expected_cash), CurrencyUtils.format(expected))
        
        val actual = shift.endActualCash ?: java.math.BigDecimal.ZERO
        formatLine(context.getString(R.string.receipt_actual_cash), CurrencyUtils.format(actual))
        
        val discrepancy = actual.subtract(expected)
        val discrepancyLabel = if (discrepancy < java.math.BigDecimal.ZERO) context.getString(R.string.receipt_shortage) else context.getString(R.string.receipt_overage)
        formatLine(discrepancyLabel + ":", CurrencyUtils.format(discrepancy.abs()), isBold = true)

        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(1))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_signature), Alignment.CENTER))
        commands.add(PrintCommand.Feed(2))
        commands.add(PrintCommand.Text("____________________", Alignment.CENTER))
        commands.add(PrintCommand.Text(shift.staffName, Alignment.CENTER))

        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }

    fun generateEodReport(
        context: Context,
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

        commands.add(PrintCommand.Header(context.getString(R.string.receipt_eod_summary)))
        commands.add(PrintCommand.Text(config.storeName, Alignment.CENTER))
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text(context.getString(R.string.receipt_business_date, sdf.format(Date(eod.businessDate)))))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_generated_by, eod.staffName)))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_shifts_closed, eod.shiftCount.toString())))
        commands.add(PrintCommand.Divider)

        commands.add(PrintCommand.Text(context.getString(R.string.receipt_sales_summary), isBold = true))
        formatLine(context.getString(R.string.receipt_gross_sales), CurrencyUtils.format(eod.grossSales))
        formatLine(context.getString(R.string.receipt_total_discount), "-${CurrencyUtils.format(eod.totalDiscount)}")
        formatLine(context.getString(R.string.receipt_total_service_charge), CurrencyUtils.format(eod.totalServiceCharge))
        formatLine(context.getString(R.string.receipt_total_tax), CurrencyUtils.format(eod.totalTax))
        formatLine(context.getString(R.string.receipt_rounding), CurrencyUtils.format(eod.totalRounding))
        
        commands.add(PrintCommand.Divider)
        formatLine(context.getString(R.string.receipt_net_sales), CurrencyUtils.format(eod.netSales), isBold = true)
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_payment_breakdown), isBold = true))
        formatLine(context.getString(R.string.receipt_cash), CurrencyUtils.format(eod.totalCashSales))
        formatLine(context.getString(R.string.receipt_other_payment), CurrencyUtils.format(eod.totalOtherSales))

        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }

    fun generateOrderSlip(
        context: Context,
        saleId: String,
        tableName: String?,
        items: List<SaleItem>,
        tag: String
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        
        commands.add(PrintCommand.Header(context.getString(R.string.receipt_order_tag, tag)))
        tableName?.let { 
            commands.add(PrintCommand.BigText(context.getString(R.string.receipt_table_name, it), Alignment.CENTER)) 
        }
        commands.add(PrintCommand.BigText(context.getString(R.string.receipt_order_id, saleId.takeLast(6)), Alignment.CENTER))
        commands.add(PrintCommand.Text(context.getString(R.string.receipt_time, sdf.format(Date())), Alignment.CENTER))
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

    fun generateTableQrSticker(context: Context, tableName: String, qrContent: String): List<PrintCommand> {
        return listOf(
            PrintCommand.Header(context.getString(R.string.receipt_scan_to_order)),
            PrintCommand.BigText(tableName, Alignment.CENTER),
            PrintCommand.Divider,
            PrintCommand.Feed(1),
            PrintCommand.QRCode(qrContent),
            PrintCommand.Feed(1),
            PrintCommand.Text(context.getString(R.string.receipt_branch_main), Alignment.CENTER),
            PrintCommand.Text(context.getString(R.string.receipt_powered_by), Alignment.CENTER),
            PrintCommand.Feed(3),
            PrintCommand.Cut
        )
    }
}
