package com.extrotarget.extroposv2.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import com.extrotarget.extroposv2.core.data.model.inventory.StockMovement
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.extrotarget.extroposv2.ui.components.barcode.BarcodeScannerView
import com.extrotarget.extroposv2.ui.inventory.components.AddEditProductDialog
import com.extrotarget.extroposv2.ui.inventory.components.ImportCsvDialog
import com.extrotarget.extroposv2.ui.inventory.components.InventoryProductItem
import com.extrotarget.extroposv2.ui.inventory.viewmodel.InventoryViewModel
import com.extrotarget.extroposv2.ui.util.CameraPermissionWrapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateToStockTransfer: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showScanner by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val result = viewModel.exportProducts(outputStream)
                        if (result.isSuccess) {
                            Toast.makeText(context, "Exported ${result.getOrNull()} products", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        containerColor = Color(0xFFF1F5F9), // Slate 100
        topBar = {
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "INVENTORY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InventoryActionButton(
                                icon = Icons.Default.FileDownload,
                                label = "Export",
                                onClick = {
                                    val fileName = "products_export_${System.currentTimeMillis()}.csv"
                                    exportLauncher.launch(fileName)
                                }
                            )
                            InventoryActionButton(
                                icon = Icons.Default.FileUpload,
                                label = "Import",
                                onClick = { showImportDialog = true }
                            )
                            InventoryActionButton(
                                icon = Icons.Default.Sync,
                                label = "Transfer",
                                onClick = onNavigateToStockTransfer
                            )
                            InventoryActionButton(
                                icon = Icons.Default.QrCodeScanner,
                                label = "Scan",
                                onClick = { showScanner = true },
                                containerColor = Color(0xFF3B82F6),
                                contentColor = Color.White
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("NEW PRODUCT", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        val lowStockProducts by viewModel.lowStockProducts.collectAsState()
        
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Stats & Search Bar Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InventoryStatCard(
                        modifier = Modifier.weight(1f),
                        label = "TOTAL PRODUCTS",
                        value = "${uiState.filteredProducts.size}",
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        color = Color(0xFF3B82F6)
                    )
                    InventoryStatCard(
                        modifier = Modifier.weight(1f),
                        label = "LOW STOCK",
                        value = "${lowStockProducts.size}",
                        icon = Icons.Default.WarningAmber,
                        color = if (lowStockProducts.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search by name, SKU or scan barcode...",
                            color = Color(0xFF94A3B8)
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            verticalAlignment = Alignment.CenterVertically,
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
fun InventoryActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = Color(0xFFF1F5F9),
    contentColor: Color = Color(0xFF475569)
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor)
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun InventoryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
            }
        }
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
