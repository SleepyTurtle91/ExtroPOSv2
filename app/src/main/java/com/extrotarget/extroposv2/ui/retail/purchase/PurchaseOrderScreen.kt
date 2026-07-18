package com.extrotarget.extroposv2.ui.retail.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.retail.purchase.viewmodel.PurchaseOrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderScreen(
    onBack: () -> Unit,
    viewModel: PurchaseOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PURCHASE ORDERS", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New PO")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8FAFC))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.orders) { po ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(po.poNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                StatusBadge(po.status.name)
                            }
                            Text("Created on ${dateFormat.format(Date(po.createdAt))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text("Total: RM ${po.totalAmount}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePoDialog(
            suppliers = uiState.suppliers,
            allProducts = uiState.allProducts,
            onDismiss = { showCreateDialog = false },
            onConfirm = { supplierId, poNumber, items ->
                viewModel.createOrder(supplierId, poNumber, items)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
