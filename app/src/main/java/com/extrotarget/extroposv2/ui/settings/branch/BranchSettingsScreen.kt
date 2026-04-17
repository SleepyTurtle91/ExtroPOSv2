package com.extrotarget.extroposv2.ui.settings.branch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.inventory.Branch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BranchSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var branchToEdit by remember { mutableStateOf<Branch?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Branch Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                branchToEdit = null
                showAddDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Branch")
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
            if (uiState.branches.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No branches configured. Add the HQ first.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            items(uiState.branches) { branch ->
                BranchItem(
                    branch = branch,
                    onEdit = { 
                        branchToEdit = it
                        showAddDialog = true 
                    },
                    onDelete = { viewModel.deleteBranch(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEditBranchDialog(
            branch = branchToEdit,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, ip, isHQ, token ->
                viewModel.saveBranch(name, ip, isHQ, token)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BranchItem(
    branch: Branch,
    onEdit: (Branch) -> Unit,
    onDelete: (Branch) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(branch.name, style = MaterialTheme.typography.titleMedium)
                    if (branch.isHQ) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("HQ") },
                            leadingIcon = { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                Text("IP: ${branch.ipAddress}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onEdit(branch) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(branch) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddEditBranchDialog(
    branch: Branch?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, String?) -> Unit
) {
    var name by remember { mutableStateOf(branch?.name ?: "") }
    var ipAddress by remember { mutableStateOf(branch?.ipAddress ?: "") }
    var isHQ by remember { mutableStateOf(branch?.isHQ ?: false) }
    var syncToken by remember { mutableStateOf(branch?.syncToken ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (branch == null) "Add Branch" else "Edit Branch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Branch Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("HQ IP Address / Domain") },
                    placeholder = { Text("e.g. 1.2.3.4:8080") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isHQ, onCheckedChange = { isHQ = it })
                    Text("This is the HQ Branch (Central Relay)")
                }
                OutlinedTextField(
                    value = syncToken,
                    onValueChange = { syncToken = it },
                    label = { Text("Sync Token (Security)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, ipAddress, isHQ, syncToken.takeIf { it.isNotBlank() }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
