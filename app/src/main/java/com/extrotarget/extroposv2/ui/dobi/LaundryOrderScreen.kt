package com.extrotarget.extroposv2.ui.dobi

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryStatus
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.dobi.viewmodel.LaundryViewModel
import java.math.BigDecimal
import java.net.URLEncoder
import java.util.UUID

@Composable
fun LaundryOrderScreen(
    viewModel: LaundryViewModel
) {
    val orders by viewModel.orders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Laundry Order")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "Laundry Orders",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders) { order ->
                    LaundryOrderCard(
                        order = order,
                        onStatusChange = { newStatus -> viewModel.updateStatus(order, newStatus) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddLaundryOrderDialog(
            pricePerKg = uiState.pricePerKg,
            liveWeight = uiState.liveWeight,
            onTare = viewModel::tareScale,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, weight, note ->
                viewModel.createOrder(name, phone, weight, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LaundryOrderCard(
    order: LaundryOrder,
    onStatusChange: (LaundryStatus) -> Unit
) {
    val context = LocalContext.current
    val statusColor = when (order.status) {
        LaundryStatus.RECEIVED -> Color.Gray
        LaundryStatus.PROCESSING -> MaterialTheme.colorScheme.primary
        LaundryStatus.READY -> Color(0xFF4CAF50) // Green
        LaundryStatus.COLLECTED -> Color.DarkGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.customerPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = statusColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = order.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Weight: ${order.weightKg} KG", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = CurrencyUtils.format(order.totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (order.status != LaundryStatus.COLLECTED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (order.customerPhone.isNotBlank()) {
                        IconButton(onClick = {
                            val message = "Hello ${order.customerName}, your laundry order #${order.id.takeLast(4)} is ${order.status.name.lowercase()}. Total: ${CurrencyUtils.format(order.totalPrice)}. Thank you!"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=${order.customerPhone}&text=${URLEncoder.encode(message, "UTF-8")}")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Whatsapp, contentDescription = "WhatsApp Notification", tint = Color(0xFF25D366))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (order.status == LaundryStatus.RECEIVED || order.status == LaundryStatus.PROCESSING) {
                        Button(onClick = { onStatusChange(LaundryStatus.READY) }) {
                            Text("Mark Ready")
                        }
                    } else if (order.status == LaundryStatus.READY) {
                        Button(
                            onClick = { onStatusChange(LaundryStatus.COLLECTED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Collect")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddLaundryOrderDialog(
    pricePerKg: BigDecimal,
    liveWeight: BigDecimal = BigDecimal.ZERO,
    onTare: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (String, String, BigDecimal, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val calculatedTotal = remember(weight) {
        val w = weight.toBigDecimalOrNull() ?: BigDecimal.ZERO
        w.multiply(pricePerKg)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Laundry Order") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Live Scale Reading Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Live Scale Reading", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${liveWeight.setScale(2, java.math.RoundingMode.HALF_EVEN)} KG",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Row {
                            TextButton(onClick = onTare) {
                                Text("TARE")
                            }
                            Button(onClick = { weight = liveWeight.toString() }) {
                                Text("CAPTURE")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (KG)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Text("KG", modifier = Modifier.padding(end = 8.dp)) }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Price:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            CurrencyUtils.format(calculatedTotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val w = weight.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    if (name.isNotBlank() && w > BigDecimal.ZERO) {
                        onConfirm(name, phone, w, note)
                    }
                }
            ) {
                Text("Create Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}