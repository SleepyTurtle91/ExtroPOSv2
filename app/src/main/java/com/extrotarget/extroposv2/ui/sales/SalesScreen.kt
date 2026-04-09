package com.extrotarget.extroposv2.ui.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Print
import com.extrotarget.extroposv2.ui.components.qr.QrCodeView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.components.CartItemRow
import com.extrotarget.extroposv2.ui.components.ProductCard
import com.extrotarget.extroposv2.ui.components.barcode.BarcodeScannerView
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import com.extrotarget.extroposv2.ui.sales.components.DiscountDialog
import com.extrotarget.extroposv2.ui.util.CameraPermissionWrapper

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showScanner by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxSize()) {
        // Left Side: Product Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Products",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                IconButton(onClick = { showScanner = true }) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan to Add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.products) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        // Right Side: Cart
        Surface(
            modifier = Modifier
                .width(400.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Current Cart",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cartItems) { item ->
                        CartItemRow(
                            cartItem = item,
                            onIncreaseQuantity = { viewModel.updateQuantity(item, item.quantity.add(java.math.BigDecimal.ONE)) },
                            onDecreaseQuantity = { viewModel.updateQuantity(item, item.quantity.subtract(java.math.BigDecimal.ONE)) },
                            onRemoveItem = { viewModel.removeFromCart(item) },
                            onApplyDiscount = { viewModel.showItemDiscountDialog(item) },
                            modifier = Modifier.clickable { viewModel.showModifierSelection(item) }
                        )
                        if (item.modifiers.isNotEmpty()) {
                            Text(
                                text = item.modifiers.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Totals
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyUtils.format(uiState.subtotal))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.showCartDiscountDialog() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Discount,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Discount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            }
                            Text(
                                "- ${CurrencyUtils.format(uiState.totalDiscount)}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax", style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyUtils.format(uiState.totalTax))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rounding", style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyUtils.format(uiState.roundingAdjustment))
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                CurrencyUtils.format(uiState.totalAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.completeSale("QR") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.cartItems.isNotEmpty() && !uiState.isCheckingOut,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("DuitNow QR")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.completeSale("CASH") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.cartItems.isNotEmpty() && !uiState.isCheckingOut,
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (uiState.isCheckingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "PAY ${CurrencyUtils.format(uiState.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (uiState.showDiscountDialog) {
        DiscountDialog(
            initialDiscount = if (uiState.itemAwaitingDiscount != null) uiState.itemAwaitingDiscount!!.discount else uiState.cartDiscount,
            onDismiss = { viewModel.dismissDiscountDialog() },
            onApply = { viewModel.applyDiscount(it) }
        )
    }

    if (uiState.showPaymentSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPaymentSuccess() },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Payment Successful", style = MaterialTheme.typography.headlineSmall)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Receipt ID: ${uiState.lastSaleId?.takeLast(8)}")
                    
                    if (uiState.lastSaleQrContent != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("DuitNow QR", style = MaterialTheme.typography.titleMedium)
                        QrCodeView(
                            content = uiState.lastSaleQrContent!!,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text("Scan to Pay", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissPaymentSuccess() }) {
                    Text("Done")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    // ViewModel already has reprint logic
                    // viewModel.reprintLastReceipt(...) 
                }) {
                    Text("Reprint Receipt")
                }
            }
        )
    }

    if (showScanner) {
        // ... Scanner Logic
    }

    if (uiState.itemAwaitingModifiers != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissModifierSelection() },
            title = { Text("Modifiers for ${uiState.itemAwaitingModifiers!!.product.name}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    uiState.availableModifiers.forEach { modifier ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleModifier(modifier) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.itemAwaitingModifiers!!.modifiers.contains(modifier),
                                onCheckedChange = { viewModel.toggleModifier(modifier) }
                            )
                            Text(text = modifier, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissModifierSelection() }) {
                    Text("Done")
                }
            }
        )
    }

    if (uiState.showStaffSelection && uiState.itemAwaitingStaff != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelStaffSelection() },
            title = { Text("Assign Staff to ${uiState.itemAwaitingStaff!!.product.name}") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(uiState.staffList) { staff ->
                        ListItem(
                            headlineContent = { Text(staff.name) },
                            supportingContent = { Text(staff.role) },
                            modifier = Modifier.clickable { viewModel.assignStaffToItem(staff) },
                            trailingContent = {
                                RadioButton(
                                    selected = false, // Not needed as it clicks and closes
                                    onClick = { viewModel.assignStaffToItem(staff) }
                                )
                            }
                        )
                    }
                    if (uiState.staffList.isEmpty()) {
                        item {
                            Text(
                                "No active staff found. Please add staff in settings.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.cancelStaffSelection() }) {
                    Text("Cancel")
                }
            }
        )
    }
}
