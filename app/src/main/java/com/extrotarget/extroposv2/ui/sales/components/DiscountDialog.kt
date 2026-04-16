package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.sales.Discount
import com.extrotarget.extroposv2.ui.sales.DiscountType
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountDialog(
    initialDiscount: Discount?,
    onDismiss: () -> Unit,
    onApply: (Discount?) -> Unit
) {
    var discountType by remember { mutableStateOf(initialDiscount?.type ?: DiscountType.PERCENTAGE) }
    var discountValue by remember { mutableStateOf(initialDiscount?.value?.toPlainString() ?: "") }
    var discountLabel by remember { mutableStateOf(initialDiscount?.label ?: "") }
    val currencySymbol = remember { CurrencyUtils.getCurrencySymbol() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Discount") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Discount Type Toggle
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = discountType == DiscountType.PERCENTAGE,
                        onClick = { discountType = DiscountType.PERCENTAGE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("%")
                    }
                    SegmentedButton(
                        selected = discountType == DiscountType.FIXED,
                        onClick = { discountType = DiscountType.FIXED },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(currencySymbol)
                    }
                }

                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) discountValue = it },
                    label = { Text("Value") },
                    suffix = { Text(if (discountType == DiscountType.PERCENTAGE) "%" else currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = discountLabel,
                    onValueChange = { discountLabel = it },
                    label = { Text("Label (e.g. Staff Discount)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (initialDiscount != null) {
                    TextButton(
                        onClick = { onApply(null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Discount")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = discountValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    if (value > BigDecimal.ZERO) {
                        onApply(Discount(discountType, value, discountLabel.ifEmpty { null }))
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
