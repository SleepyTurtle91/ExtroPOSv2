package com.extrotarget.extroposv2.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.report.viewmodel.SalesHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    viewModel: SalesHistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showVoidDialog by remember { mutableStateOf<SaleWithItems?>(null) }
    var voidReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search by Sale ID or Product...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (uiState.sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.sales) { saleWithItems ->
                        SaleHistoryItem(
                            saleWithItems = saleWithItems,
                            onVoid = { showVoidDialog = saleWithItems },
                            onReprint = { viewModel.reprintReceipt(saleWithItems) }
                        )
                    }
                }
            }
        }
    }

    if (showVoidDialog != null) {
        AlertDialog(
            onDismissRequest = { showVoidDialog = null; voidReason = "" },
            title = { Text("Void Transaction?") },
            text = {
                Column {
                    Text("Are you sure you want to void Sale #${showVoidDialog!!.sale.id.takeLast(6)}?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it },
                        label = { Text("Reason for Voiding") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.voidSale(showVoidDialog!!.sale.id, voidReason)
                        showVoidDialog = null
                        voidReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = voidReason.isNotBlank()
                ) {
                    Text("VOID SALE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoidDialog = null; voidReason = "" }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun SaleHistoryItem(
    saleWithItems: SaleWithItems,
    onVoid: () -> Unit,
    onReprint: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val isVoided = saleWithItems.sale.status == "VOIDED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isVoided) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sale #${saleWithItems.sale.id.takeLast(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(saleWithItems.sale.timestamp)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Surface(
                    color = if (isVoided) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isVoided) "VOIDED" else saleWithItems.sale.paymentMethod,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isVoided) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            saleWithItems.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.quantity.toInt()}x ${item.productName}", style = MaterialTheme.typography.bodyMedium)
                    Text(CurrencyUtils.format(item.totalAmount), style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (saleWithItems.sale.roundingAdjustment != java.math.BigDecimal.ZERO) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rounding", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(CurrencyUtils.format(saleWithItems.sale.roundingAdjustment), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL", fontWeight = FontWeight.Black)
                Text(CurrencyUtils.format(saleWithItems.sale.totalAmount), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }

            if (!isVoided) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onVoid, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("VOID")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onReprint) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("REPRINT")
                    }
                }
            }
        }
    }
}
