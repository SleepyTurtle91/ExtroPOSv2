package com.extrotarget.extroposv2.ui.settings.autocount

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCountSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AutoCountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoCount Sync Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveConfig() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enable AutoCount Sync",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = uiState.config.isEnabled,
                            onCheckedChange = { viewModel.onConfigChange(uiState.config.copy(isEnabled = it)) }
                        )
                    }
                    Text(
                        text = "When enabled, sales will be automatically synced to AutoCount Accounting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = uiState.config.apiUrl,
                onValueChange = { viewModel.onConfigChange(uiState.config.copy(apiUrl = it)) },
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("http://192.168.1.100:8080/") }
            )

            OutlinedTextField(
                value = uiState.config.username,
                onValueChange = { viewModel.onConfigChange(uiState.config.copy(username = it)) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.config.password,
                onValueChange = { viewModel.onConfigChange(uiState.config.copy(password = it)) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            Text(
                text = "Account Mapping",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.config.cashAccountCode,
                onValueChange = { viewModel.onConfigChange(uiState.config.copy(cashAccountCode = it)) },
                label = { Text("Cash Account Code") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("300-000") }
            )

            OutlinedTextField(
                value = uiState.config.cardAccountCode,
                onValueChange = { viewModel.onConfigChange(uiState.config.copy(cardAccountCode = it)) },
                label = { Text("Card/E-Wallet Account Code") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("300-001") }
            )

            OutlinedTextField(
                value = uiState.config.defaultTaxCode,
                onValueChange = { viewModel.onConfigChange(uiState.config.copy(defaultTaxCode = it)) },
                label = { Text("Default Tax Code") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("SR-S") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Connection & Get Token")
                }
            }

            if (uiState.config.syncToken != null) {
                Text(
                    text = "Token: ${uiState.config.syncToken?.take(20)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            uiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            uiState.successMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
