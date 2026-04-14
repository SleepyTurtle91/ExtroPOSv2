package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightInputDialog(
    product: Product,
    onConfirm: (BigDecimal) -> Unit,
    onDismiss: () -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    val weight = weightText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val totalPrice = product.price.multiply(weight)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Weight for ${product.name}") },
        text = {
            Column {
                Text(
                    "Price: ${CurrencyUtils.format(product.price)} / KG",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (KG)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("KG") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (weight > BigDecimal.ZERO) {
                    Text(
                        "Total: ${CurrencyUtils.format(totalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (weight > BigDecimal.ZERO) onConfirm(weight) },
                enabled = weight > BigDecimal.ZERO
            ) {
                Text("Add to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
