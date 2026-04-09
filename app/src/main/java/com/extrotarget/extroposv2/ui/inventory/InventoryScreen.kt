package com.extrotarget.extroposv2.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import com.extrotarget.extroposv2.ui.components.barcode.BarcodeScannerView
import com.extrotarget.extroposv2.ui.inventory.components.AddEditProductDialog
import com.extrotarget.extroposv2.ui.inventory.components.ImportCsvDialog
import com.extrotarget.extroposv2.ui.inventory.components.InventoryProductItem
import com.extrotarget.extroposv2.ui.inventory.viewmodel.InventoryViewModel
import com.extrotarget.extroposv2.ui.util.CameraPermissionWrapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showScanner by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management") },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Bulk Import")
                    }
                    IconButton(onClick = { showScanner = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddProductDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        val lowStockProducts by viewModel.lowStockProducts.collectAsState()
        
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (lowStockProducts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Low Stock Alert (${lowStockProducts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        lowStockProducts.take(3).forEach { product ->
                            Text(
                                "• ${product.name}: ${product.stockQuantity} left (Min: ${product.minStockLevel})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search products (Name, SKU, Barcode)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(uiState.filteredProducts) { product ->
                    InventoryProductItem(
                        product = product,
                        onProductClick = { viewModel.selectProduct(it) }
                    )
                }
            }
        }
    }

    if (showScanner) {
        CameraPermissionWrapper(
            onPermissionGranted = {
                AlertDialog(
                    onDismissRequest = { showScanner = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier.fillMaxSize(),
                    title = {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scan Product Barcode", modifier = Modifier.weight(1f))
                            IconButton(onClick = { showScanner = false }) {
                                Icon(Icons.Default.Add, modifier = Modifier.rotate(45f), contentDescription = "Close")
                            }
                        }
                    },
                    text = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            BarcodeScannerView(
                                onBarcodeDetected = { barcode ->
                                    viewModel.onSearchQueryChange(barcode)
                                    showScanner = false
                                }
                            )
                        }
                    },
                    confirmButton = {}
                )
            },
            onPermissionDenied = {
                showScanner = false
            }
        )
    }

    if (showImportDialog) {
        ImportCsvDialog(
            viewModel = hiltViewModel(),
            onDismiss = { showImportDialog = false },
            onImportSuccess = { 
                showImportDialog = false
                // refresh could be handled by flow
            }
        )
    }

    if (showAddProductDialog) {
        AddEditProductDialog(
            categories = uiState.categories,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { 
                viewModel.upsertProduct(it)
                showAddProductDialog = false
            }
        )
    }

    if (uiState.selectedProduct != null) {
        StockAdjustmentDialog(
            product = uiState.selectedProduct!!,
            movements = uiState.stockMovements,
            onDismiss = { viewModel.selectProduct(null) },
            onConfirm = { quantity, type, note ->
                viewModel.adjustStock(quantity, type, note)
                viewModel.selectProduct(null)
            },
            onSetStock = { quantity, type, note ->
                viewModel.setStock(quantity, type, note)
                viewModel.selectProduct(null)
            }
        )
    }
}

@Composable
fun StockAdjustmentDialog(
    product: com.extrotarget.extroposv2.core.data.model.Product,
    movements: List<StockMovement>,
    onDismiss: () -> Unit,
    onConfirm: (java.math.BigDecimal, String, String?) -> Unit,
    onSetStock: (java.math.BigDecimal, String, String?) -> Unit
) {
    var quantityStr by remember { mutableStateOf("") }
    var adjustmentType by remember { mutableStateOf("IN") } // IN, OUT, SET
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f),
        title = { Text("Stock Management: ${product.name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Current Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text(if (adjustmentType == "SET") "New Stock Level" else "Adjustment Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = adjustmentType == "IN", onClick = { adjustmentType = "IN" })
                        Text("Add", modifier = Modifier.padding(start = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = adjustmentType == "OUT", onClick = { adjustmentType = "OUT" })
                        Text("Reduce", modifier = Modifier.padding(start = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = adjustmentType == "SET", onClick = { adjustmentType = "SET" })
                        Text("Set Total", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reason / Reference (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("Recent Audit Logs", style = MaterialTheme.typography.titleSmall)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Box(modifier = Modifier.height(200.dp)) {
                    LazyColumn {
                        items(movements) { movement ->
                            StockMovementRow(movement)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                    if (adjustmentType == "SET") {
                        onSetStock(qty, "ADJUSTMENT", note)
                    } else {
                        val finalQty = if (adjustmentType == "OUT") qty.negate() else qty
                        onConfirm(finalQty, adjustmentType, note)
                    }
                }
            ) {
                Text("Update Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun StockMovementRow(movement: StockMovement) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()) }
    val color = when {
        movement.type == "SALE" || movement.quantity < java.math.BigDecimal.ZERO -> Color.Red
        movement.quantity > java.math.BigDecimal.ZERO -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${movement.type}: ${if (movement.quantity > java.math.BigDecimal.ZERO) "+" else ""}${movement.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(Date(movement.timestamp)),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (!movement.note.isNullOrBlank()) {
            Text(
                text = movement.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}
