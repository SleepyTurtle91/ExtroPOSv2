package com.extrotarget.extroposv2.ui.settings.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val localIp by viewModel.localIp.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val discoveredMasters by viewModel.discoveredMasters.collectAsState()

    var masterIp by remember { mutableStateOf("") }
    var showSyncDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Terminal Sync (Master/Slave)") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Role Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isServerRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Terminal Role: Master", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Switch(checked = isServerRunning, onCheckedChange = { viewModel.toggleServer() })
                    }
                    Text("Enable this on the primary terminal to allow other devices (Slaves) to sync data.", style = MaterialTheme.typography.bodySmall)
                    
                    if (isServerRunning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Local Network Address:", style = MaterialTheme.typography.labelSmall)
                        Text(localIp, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Slave Role Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Terminal Role: Slave", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Download data from the Master terminal to keep this device up to date.", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = masterIp,
                        onValueChange = { masterIp = it },
                        label = { Text("Master Terminal IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isServerRunning,
                        placeholder = { Text("e.g. 192.168.1.100") }
                    )
                    
                    if (discoveredMasters.isNotEmpty()) {
                        Text("Discovered Master Terminals:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 16.dp))
                        discoveredMasters.forEach { service ->
                            AssistChip(
                                onClick = { masterIp = service.host.hostAddress ?: "" },
                                label = { Text("${service.serviceName} (${service.host.hostAddress})") },
                                leadingIcon = { Icon(Icons.Default.CastConnected, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.syncFromMaster(masterIp) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        enabled = masterIp.isNotEmpty() && !isServerRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync from Master")
                    }
                }
            }

            if (syncStatus != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(syncStatus!!, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "Note: Both devices must be on the same local network. Syncing will overwrite local data with data from Master.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
