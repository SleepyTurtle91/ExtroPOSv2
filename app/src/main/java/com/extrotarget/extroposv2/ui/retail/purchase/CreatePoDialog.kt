package com.extrotarget.extroposv2.ui.retail.purchase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.domain.retail.model.Supplier
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.domain.retail.model.PurchaseOrderItem
import java.math.BigDecimal
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePoDialog(
    suppliers: List<Supplier>,
    allProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<PurchaseOrderItem>) -> Unit
) {
    var selectedSupplierId by remember { mutableStateOf("") }
    var poNumber by remember { mutableStateOf("PO-${System.currentTimeMillis()/10000}") }
    val orderItems = remember { mutableStateListOf<PurchaseOrderItem>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
        title = { Text("Create Purchase Order", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. Supplier Selection
                Text("Supplier", style = MaterialTheme.typography.labelMedium)
                suppliers.forEach { supplier ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedSupplierId == supplier.id, onClick = { selectedSupplierId = supplier.id })
                        Text(supplier.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                OutlinedTextField(value = poNumber, onValueChange = { poNumber = it }, label = { Text("PO Number") }, modifier = Modifier.fillMaxWidth())

                Divider()

                // 2. Item List
                Text("Order Items", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(orderItems) { item ->
                        val productName = allProducts.find { it.id == item.productId }?.name ?: "Unknown"
                        ListItem(
                            headlineContent = { Text(productName) },
                            supportingContent = { Text("Qty: ${item.quantity} | Cost: RM ${item.unitCost}") },
                            trailingContent = {
                                IconButton(onClick = { orderItems.remove(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        )
                    }
                }

                // 3. Add Item Helper (Simplified)
                Button(onClick = {
                    // Just add a dummy item for now or trigger another picker
                    if (allProducts.isNotEmpty()) {
                        orderItems.add(
                            PurchaseOrderItem(
                                id = UUID.randomUUID().toString(),
                                poId = "",
                                productId = allProducts.first().id,
                                quantity = BigDecimal.ONE,
                                unitCost = allProducts.first().price.multiply(BigDecimal("0.7")) // 30% margin default
                            )
                        )
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("ADD ITEM")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedSupplierId, poNumber, orderItems.toList()) },
                enabled = selectedSupplierId.isNotEmpty() && orderItems.isNotEmpty()
            ) {
                Text("GENERATE PO")
            }
        }
    )
}
