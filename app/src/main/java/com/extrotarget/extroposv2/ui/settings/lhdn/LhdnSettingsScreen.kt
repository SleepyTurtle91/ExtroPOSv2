package com.extrotarget.extroposv2.ui.settings.lhdn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LhdnSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: LhdnSettingsViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    val secureClientId by viewModel.clientId.collectAsState()
    val secureClientSecret by viewModel.clientSecret.collectAsState()

    var sellerTin by remember(config) { mutableStateOf(config?.sellerTin ?: "") }
    var sellerBrn by remember(config) { mutableStateOf(config?.sellerBrn ?: "") }
    var sellerSstId by remember(config) { mutableStateOf(config?.sellerSstId ?: "") }
    var msicCode by remember(config) { mutableStateOf(config?.msicCode ?: "") }
    var businessDesc by remember(config) { mutableStateOf(config?.businessActivityDesc ?: "") }
    var clientId by remember(secureClientId) { mutableStateOf(secureClientId) }
    var clientSecret by remember(secureClientSecret) { mutableStateOf(secureClientSecret) }
    var isSandbox by remember(config) { mutableStateOf(config?.isSandbox ?: true) }
    var isEnabled by remember(config) { mutableStateOf(config?.isEnabled ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LHDN MyInvois Config") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Submission History")
                    }
                    IconButton(onClick = {
                        viewModel.saveConfig(
                            sellerTin, sellerBrn, sellerSstId, msicCode, businessDesc, 
                            clientId, clientSecret, isSandbox, isEnabled
                        )
                        onNavigateBack()
                    }) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                Spacer(Modifier.width(8.dp))
                Text("Enable LHDN MyInvois Submission")
            }

            Divider()

            Text("Merchant Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            OutlinedTextField(
                value = sellerTin,
                onValueChange = { sellerTin = it },
                label = { Text("Seller TIN") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sellerBrn,
                onValueChange = { sellerBrn = it },
                label = { Text("Registration No. (BRN)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sellerSstId,
                onValueChange = { sellerSstId = it },
                label = { Text("SST ID (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = msicCode,
                onValueChange = { msicCode = it },
                label = { Text("MSIC Code") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = businessDesc,
                onValueChange = { businessDesc = it },
                label = { Text("Business Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()
            Text("API Credentials (ERP/POS)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = clientId,
                onValueChange = { clientId = it },
                label = { Text("Client ID") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clientSecret,
                onValueChange = { clientSecret = it },
                label = { Text("Client Secret") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isSandbox, onCheckedChange = { isSandbox = it })
                Text("Enable Sandbox (Pre-Production)")
            }
        }
    }
}
