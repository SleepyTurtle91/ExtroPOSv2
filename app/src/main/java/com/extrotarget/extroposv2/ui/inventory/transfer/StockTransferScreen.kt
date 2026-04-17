package com.extrotarget.extroposv2.ui.inventory.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransfer
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(
    onNavigateBack: () -> Unit,
    viewModel: StockTransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedToBranchId by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Transfer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncStockWithHQ() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Stock")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Transfer Form
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("New Inter-Branch Transfer", style = MaterialTheme.typography.titleMedium)
                    
                    // To Branch Dropdown (Simplified for brevity)
                    OutlinedTextField(
                        value = selectedToBranchId,
                        onValueChange = { selectedToBranchId = it },
                        label = { Text("To Branch ID (Manual for now)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = selectedProductId,
                        onValueChange = { selectedProductId = it },
                        label = { Text("Product ID / SKU") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        )
                    )

                    Button(
                        onClick = {
                            val product = uiState.products.find { it.id == selectedProductId || it.sku == selectedProductId }
                            if (product != null) {
                                viewModel.createTransfer(
                                    fromBranchId = "LOCAL",
                                    toBranchId = selectedToBranchId,
                                    productId = product.id,
                                    productName = product.name,
                                    quantity = quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                )
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Initiate Transfer")
                    }
                }
            }

            Text("Transfer History", style = MaterialTheme.typography.titleMedium)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.transfers) { transfer ->
                    TransferHistoryItem(transfer)
                }
            }
        }
    }
}

@Composable
fun TransferHistoryItem(transfer: StockTransfer) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(transfer.productName, style = MaterialTheme.typography.bodyLarge)
                Text("To: ${transfer.toBranchId}", style = MaterialTheme.typography.bodySmall)
                Text(dateFormat.format(Date(transfer.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            Text("Qty: ${transfer.quantity}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
