package com.extrotarget.extroposv2.ui.loyalty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import com.extrotarget.extroposv2.core.data.model.loyalty.MemberWithHistory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    viewModel: MemberViewModel = hiltViewModel(),
    onMemberSelected: (Member) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val memberHistory by viewModel.memberHistory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Member Management") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.members) { member ->
                    ListItem(
                        modifier = Modifier.clickable { 
                            onMemberSelected(member)
                        },
                        headlineContent = { Text(member.name) },
                        supportingContent = { Text(member.phoneNumber) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${member.totalPoints.toInt()} pts",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        member.tier,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                IconButton(onClick = { 
                                    viewModel.selectMember(member.id)
                                    showHistoryDialog = true 
                                }) {
                                    Icon(Icons.Default.History, contentDescription = "View History")
                                }
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, email ->
                viewModel.registerMember(name, phone, email)
                showAddDialog = false
            }
        )
    }

    if (showHistoryDialog && memberHistory != null) {
        MemberHistoryDialog(
            history = memberHistory!!,
            onDismiss = { 
                showHistoryDialog = false
                viewModel.selectMember(null)
            }
        )
    }
}

@Composable
fun MemberHistoryDialog(
    history: MemberWithHistory,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(history.member.name)
                Text(
                    "Tier: ${history.member.tier} | Total: ${history.member.totalPoints.toInt()} pts",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                TabRow(selectedTabIndex = 0) { // Future work: multiple tabs for sales vs points
                    Tab(selected = true, onClick = {}, text = { Text("History") })
                }
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "Recent Activity",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(history.transactions) { transaction ->
                        ListItem(
                            headlineContent = { Text(transaction.type) },
                            supportingContent = { 
                                Text("${dateFormat.format(Date(transaction.timestamp))} - ${transaction.note ?: ""}") 
                            },
                            trailingContent = {
                                Text(
                                    "${if (transaction.points > BigDecimal.ZERO) "+" else ""}${transaction.points.toInt()}",
                                    color = if (transaction.points > BigDecimal.ZERO) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone, email.ifBlank { null }) },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
