package com.extrotarget.extroposv2.ui.settings.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.settings.PaymentMethod
import com.extrotarget.extroposv2.core.data.model.settings.PaymentMethodType
import com.extrotarget.extroposv2.ui.settings.payment.viewmodel.PaymentMethodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSettingsScreen(
    viewModel: PaymentMethodViewModel = hiltViewModel(),
    onNavigateToDuitNow: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newMethodName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Payment Methods") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Payment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDuitNow() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    ListItem(
                        headlineContent = { Text("Configure DuitNow QR") },
                        supportingContent = { Text("Set Merchant ID and Name for dynamic QR") },
                        leadingContent = { Icon(Icons.Default.QrCode, contentDescription = null) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Other Payment Methods", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(uiState.paymentMethods) { method ->
                PaymentMethodItem(
                    method = method,
                    onToggle = { viewModel.togglePaymentMethod(method) },
                    onDelete = { viewModel.deletePaymentMethod(method) }
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Custom Payment Method") },
                text = {
                    OutlinedTextField(
                        value = newMethodName,
                        onValueChange = { newMethodName = it },
                        label = { Text("Payment Name (e.g., ShopeePay, GrabPay)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newMethodName.isNotBlank()) {
                                viewModel.addCustomPaymentMethod(newMethodName)
                                newMethodName = ""
                                showAddDialog = false
                            }
                        }
                    ) { Text("ADD") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = method.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (method.type == PaymentMethodType.CUSTOM) "Manual Entry" else "System Default",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = method.isEnabled,
                    onCheckedChange = { onToggle() }
                )
                
                if (method.type == PaymentMethodType.CUSTOM) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
