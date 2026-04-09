package com.extrotarget.extroposv2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.sales.CartItem
import java.math.BigDecimal

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onRemoveItem: () -> Unit,
    onApplyDiscount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cartItem.product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (cartItem.assignedStaffName != null) {
                Text(
                    text = "Assigned: ${cartItem.assignedStaffName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${CurrencyUtils.format(cartItem.unitPrice)} x ${cartItem.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (cartItem.discountAmount > BigDecimal.ZERO) {
                Text(
                    text = "- ${CurrencyUtils.format(cartItem.discountAmount)} (${cartItem.discount?.label ?: "Discount"})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onApplyDiscount) {
                Icon(
                    imageVector = Icons.Default.Discount,
                    contentDescription = "Apply Discount",
                    tint = if (cartItem.discount != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDecreaseQuantity) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
                text = cartItem.quantity.stripTrailingZeros().toPlainString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onIncreaseQuantity) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
            IconButton(onClick = onRemoveItem) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = CurrencyUtils.format(cartItem.totalPrice.add(cartItem.taxAmount)),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 80.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
