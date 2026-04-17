package com.extrotarget.extroposv2.core.util.printer.fnb

import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.core.hardware.printer.Alignment
import com.extrotarget.extroposv2.core.hardware.printer.PrintCommand
import java.text.SimpleDateFormat
import java.util.*

object KitchenSlipGenerator {

    fun generateSlip(
        tableName: String,
        orderId: String,
        items: List<CartItem>,
        printerTag: String // "KITCHEN" or "BAR"
    ): List<PrintCommand> {
        val commands = mutableListOf<PrintCommand>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        // Header
        commands.add(PrintCommand.Header("${printerTag} ORDER"))
        commands.add(PrintCommand.Text("Table: $tableName", alignment = Alignment.CENTER, isBold = true))
        commands.add(PrintCommand.Text("Order ID: ${orderId.takeLast(8)}", alignment = Alignment.CENTER))
        commands.add(PrintCommand.Text("Time: ${sdf.format(Date())}", alignment = Alignment.CENTER))
        commands.add(PrintCommand.Divider)

        // Filter items by tag
        val filteredItems = items.filter { it.product.printerTag == printerTag }
        
        if (filteredItems.isEmpty()) return emptyList()

        // Items List (Large Font for Kitchen)
        filteredItems.forEach { item ->
            // Use Header style for item name to make it big for kitchen staff
            commands.add(PrintCommand.Text(
                "${item.quantity.toInt()}x ${item.product.name}",
                alignment = Alignment.LEFT,
                isBold = true
            ))
            
            // Modifiers (Step 3)
            if (item.selectedModifiers.isNotEmpty()) {
                item.selectedModifiers.forEach { modifier ->
                    val priceLabel = if (modifier.priceAdjustment > java.math.BigDecimal.ZERO) {
                        " (+RM${String.format(Locale.getDefault(), "%.2f", modifier.priceAdjustment)})"
                    } else ""
                    
                    commands.add(PrintCommand.Text(
                        "  - ${modifier.name}$priceLabel",
                        alignment = Alignment.LEFT,
                        isBold = false
                    ))
                }
            }
            commands.add(PrintCommand.Feed(1))
        }

        commands.add(PrintCommand.Divider)
        commands.add(PrintCommand.Feed(3))
        commands.add(PrintCommand.Cut)

        return commands
    }
}