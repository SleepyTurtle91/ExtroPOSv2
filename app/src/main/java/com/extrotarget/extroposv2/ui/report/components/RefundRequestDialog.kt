package com.extrotarget.extroposv2.ui.report.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundRequestDialog(
    saleId: String,
    totalAmount: BigDecimal,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal, String, Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf(totalAmount.toString()) }
    var reason by remember { mutableStateOf("") }
    var restock by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Refund for #$saleId", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Refund Amount (RM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Refund") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Damaged item, customer change of mind...") }
                )

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = restock, onCheckedChange = { restock = it })
                    Text("Return items to inventory (Restock)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    onConfirm(amount, reason, restock)
                },
                enabled = reason.isNotBlank() && (amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO) > BigDecimal.ZERO
            ) {
                Text("SUBMIT REQUEST")
            }
        }
    )
}
