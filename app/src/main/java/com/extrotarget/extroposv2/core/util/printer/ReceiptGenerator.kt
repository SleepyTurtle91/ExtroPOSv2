package com.extrotarget.extroposv2.core.util.printer

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
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
        val sstLine = config.sstId?.let { "SST ID: $it" } ?: ""
        
        if (brnLine.isNotEmpty() || sstLine.isNotEmpty()) {
            val combined = listOfNotNull(config.brn?.let { "BRN: $it" }, config.sstId?.let { "SST: $it" })
                .joinToString(" | ")
            commands.add(PrintCommand.Text(combined, Alignment.CENTER))
        }

        commands.add(PrintCommand.Divider)

        // Sale Info
        commands.add(PrintCommand.Text("Receipt ID: ${sale.id.takeLast(8)}"))
        commands.add(PrintCommand.Text("Date: ${sdf.format(Date(sale.timestamp))}"))
        commands.add(PrintCommand.Divider)

        // Items
        items.forEach { item ->
            commands.add(PrintCommand.Text(item.productName, isBold = true))
            val priceLine = "${CurrencyUtils.format(item.unitPrice)} x ${item.quantity}"
            val totalLine = CurrencyUtils.format(item.totalAmount)
            commands.add(PrintCommand.Text("$priceLine  $totalLine", Alignment.RIGHT))
            if (item.discountAmount > java.math.BigDecimal.ZERO) {
                commands.add(PrintCommand.Text("  - Discount: ${CurrencyUtils.format(item.discountAmount)}", Alignment.RIGHT))
            }
        }
        commands.add(PrintCommand.Divider)

        // Totals
        val subtotal = items.fold(java.math.BigDecimal.ZERO) { acc, item ->
            acc.add(item.unitPrice.multiply(item.quantity))
        }
        commands.add(PrintCommand.Text("Subtotal: ${CurrencyUtils.format(subtotal)}", Alignment.RIGHT))

        if (sale.discountAmount > java.math.BigDecimal.ZERO) {
            commands.add(PrintCommand.Text("Discount: -${CurrencyUtils.format(sale.discountAmount)}", Alignment.RIGHT))
        }

        commands.add(PrintCommand.Text("SST (Service Tax): ${CurrencyUtils.format(sale.taxAmount)}", Alignment.RIGHT))
        
        val rounding = sale.roundingAdjustment
        if (config.showRounding && rounding != java.math.BigDecimal.ZERO) {
            commands.add(PrintCommand.Text("Rounding: ${CurrencyUtils.format(rounding)}", Alignment.RIGHT))
        }

        commands.add(PrintCommand.Text("TOTAL: ${CurrencyUtils.format(sale.totalAmount)}", Alignment.RIGHT, isBold = true))
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text("Payment Method: ${sale.paymentMethod}", Alignment.CENTER))
        config.footerMessage?.let { commands.add(PrintCommand.Text(it, Alignment.CENTER)) }
        
        // LHDN e-Invoice QR Code
        if (config.showLhdnQr && lhdnSubmission?.uuid != null) {
            commands.add(PrintCommand.Feed(1))
            commands.add(PrintCommand.Text("LHDN E-INVOICE VERIFICATION", Alignment.CENTER, isBold = true))
            
            val baseUrl = if (isSandbox) "https://preprod.myinvois.hasil.gov.my" else "https://myinvois.hasil.gov.my"
            val lhdnUrl = "$baseUrl/uuid/${lhdnSubmission.uuid}"

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

    fun generateOrderSlip(
        saleId: String,
        tableName: String?,
        items: List<SaleItem>,
        tag: String
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        
        commands.add(PrintCommand.Header("${tag} ORDER"))
        tableName?.let { commands.add(PrintCommand.Text("TABLE: $it", Alignment.CENTER, isBold = true)) }
        commands.add(PrintCommand.Text("Order: ${saleId.takeLast(6)} | Time: ${sdf.format(Date())}"))
        commands.add(PrintCommand.Divider)
        
        items.forEach { item ->
            commands.add(PrintCommand.Text("${item.quantity.stripTrailingZeros().toPlainString()}x ${item.productName}", isBold = true))
        }
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)
        
        return commands
    }
}
