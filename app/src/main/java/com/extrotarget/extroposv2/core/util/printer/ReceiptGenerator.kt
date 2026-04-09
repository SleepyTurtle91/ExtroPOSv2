package com.extrotarget.extroposv2.core.util.printer

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.hardware.printer.Alignment
import com.extrotarget.extroposv2.core.hardware.printer.PrintCommand
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

object ReceiptGenerator {
    
    fun generateSaleReceipt(
        sale: Sale,
        items: List<SaleItem>,
        shopName: String = "ExtroPOS v2 Demo",
        shopAddress: String = "Kuala Lumpur, Malaysia",
        taxId: String? = "SST-REG-12345"
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        // Header
        commands.add(PrintCommand.Header(shopName))
        commands.add(PrintCommand.Text(shopAddress, Alignment.CENTER))
        taxId?.let { commands.add(PrintCommand.Text("SST ID: $it", Alignment.CENTER)) }
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
            // Manual padding for 32-char thermal paper if needed, but here we just send both
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
        if (rounding != java.math.BigDecimal.ZERO) {
            commands.add(PrintCommand.Text("Rounding: ${CurrencyUtils.format(rounding)}", Alignment.RIGHT))
        }

        commands.add(PrintCommand.Text("TOTAL: ${CurrencyUtils.format(sale.totalAmount)}", Alignment.RIGHT, isBold = true))
        
        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Text("Payment Method: ${sale.paymentMethod}", Alignment.CENTER))
        commands.add(PrintCommand.Text("Thank You! Please Come Again.", Alignment.CENTER))
        
        // QR Code for e-Invoicing / Verification
        commands.add(PrintCommand.Feed(1))
        commands.add(PrintCommand.QRCode("https://extropos.com/verify/${sale.id}"))
        
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }
}