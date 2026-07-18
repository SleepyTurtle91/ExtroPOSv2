package com.extrotarget.extroposv2.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.domain.commerce.StockMovementType
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentDialog(
    productName: String,
    currentStock: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal, StockMovementType, String) -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StockMovementType.ADJUSTMENT) }
    var reason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val quantity = quantityText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    
    // Auto-populate reasons based on type (as per roadmap 3C)
    LaunchedEffect(selectedType) {
        if (reason.isBlank() || reason == "Restock" || reason == "Expired" || reason == "Damaged" || reason == "Customer Return") {
            reason = when (selectedType) {
                StockMovementType.RESTOCK -> "Restock"
                StockMovementType.ADJUSTMENT -> "Inventory Correction"
                StockMovementType.RETURN -> "Customer Return"
                else -> ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(R.string.inv_adjust_stock), fontWeight = FontWeight.Bold)
                Text(productName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Current Stock: $currentStock", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { 
                        if (it.isEmpty() || it.toBigDecimalOrNull() != null || it == "-") {
                            quantityText = it
                        }
                    },
                    label = { Text("Adjustment Quantity (+/-)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = {
                        if (quantity != BigDecimal.ZERO) {
                            val newStock = currentStock.add(quantity)
                            Text("New Stock will be: $newStock")
                        }
                    }
                )

                Column {
                    Text(stringResource(R.string.inv_stock_type), style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StockMovementType.values().filter { it != StockMovementType.SALE && it != StockMovementType.TRANSFER }.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.inv_stock_reason)) },
                    placeholder = { Text(stringResource(R.string.inv_stock_reason_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (quantity.abs() > BigDecimal("50")) {
                    Surface(
                        color = MaterialTheme.colorScheme.warningContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.inv_large_adj_warning),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onWarningContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (quantity == BigDecimal.ZERO) {
                        isError = true
                    } else {
                        onConfirm(quantity, selectedType, reason)
                    }
                },
                enabled = quantityText.isNotEmpty() && reason.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

// Extension to avoid missing color errors if not in theme
private val ColorScheme.warningContainer: androidx.compose.ui.graphics.Color
    @Composable get() = tertiaryContainer

private val ColorScheme.onWarningContainer: androidx.compose.ui.graphics.Color
    @Composable get() = onTertiaryContainer
