package com.extrotarget.extroposv2.ui.retail.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.retail.purchase.viewmodel.SupplierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(
    onBack: () -> Unit,
    viewModel: SupplierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SUPPLIER MANAGEMENT", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8FAFC))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.suppliers) { supplier ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(supplier.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                supplier.phone?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, email, address ->
                viewModel.saveSupplier(name, phone, email, address)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Supplier Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, phone, email, address) }, enabled = name.isNotBlank()) {
                Text("SAVE")
            }
        }
    )
}
