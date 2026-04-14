package com.extrotarget.extroposv2.ui.carwash.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.carwash.viewmodel.StaffViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    viewModel: StaffViewModel = viewModel()
) {
    val staffWithEarnings by viewModel.staffWithEarningsList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<Staff?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Staff Management & Earnings") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Staff")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(staffWithEarnings) { data ->
                StaffItem(
                    staff = data.staff,
                    totalEarnings = data.totalEarnings,
                    onEdit = { staffToEdit = it },
                    onDelete = { viewModel.deleteStaff(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        StaffDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, role, phone, pin ->
                viewModel.addStaff(name, role, phone, pin)
                showAddDialog = false
            }
        )
    }

    if (staffToEdit != null) {
        StaffDialog(
            staff = staffToEdit,
            onDismiss = { staffToEdit = null },
            onConfirm = { name, role, phone, pin ->
                viewModel.updateStaff(staffToEdit!!.copy(name = name, role = role, phone = phone, pin = pin))
                staffToEdit = null
            }
        )
    }
}

@Composable
fun StaffItem(
    staff: Staff,
    totalEarnings: java.math.BigDecimal,
    onEdit: (Staff) -> Unit,
    onDelete: (Staff) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = staff.name, style = MaterialTheme.typography.titleMedium)
                Text(text = staff.role, style = MaterialTheme.typography.bodySmall)
                staff.phone?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Earnings: ${CurrencyUtils.format(totalEarnings)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = { onEdit(staff) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(staff) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StaffDialog(
    staff: Staff? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(staff?.name ?: "") }
    var role by remember { mutableStateOf(staff?.role ?: "WASHER") }
    var phone by remember { mutableStateOf(staff?.phone ?: "") }
    var pin by remember { mutableStateOf(staff?.pin ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (staff == null) "Add Staff" else "Edit Staff") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (e.g. WASHER, SUPERVISOR, ADMIN, CASHIER)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("PIN (4-6 digits)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, role, phone.ifBlank { null }, pin.ifBlank { null }) },
                enabled = name.isNotBlank() && role.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
