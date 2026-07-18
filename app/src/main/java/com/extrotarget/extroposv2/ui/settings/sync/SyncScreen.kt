package com.extrotarget.extroposv2.ui.settings.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.network.QueueStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val localIp by viewModel.localIp.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val discoveredMasters by viewModel.discoveredMasters.collectAsState()
    val offlineQueue by viewModel.offlineQueue.collectAsState()

    var masterIp by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Terminal Sync & Offline Queue", fontWeight = FontWeight.Black) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Master Role Section
            item {
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
            }

            // Slave Role Section
            item {
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
            }

            // Offline Queue Dashboard
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OFFLINE QUEUE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.Gray)
                    IconButton(onClick = { viewModel.forceSyncQueue() }) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync Now")
                    }
                }
                
                Surface(
                    color = Color.White,
                    shape = MaterialTheme.shapes.large,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SyncStat("PENDING", offlineQueue.size.toString(), Color(0xFF3B82F6))
                            SyncStat("FAILED", offlineQueue.count { it.status == QueueStatus.FAILED }.toString(), Color(0xFFEF4444))
                        }
                    }
                }
            }

            if (offlineQueue.isNotEmpty()) {
                items(offlineQueue.take(10)) { item ->
                    ListItem(
                        headlineContent = { Text("${item.actionType} (${item.eventId.takeLast(6)})") },
                        supportingContent = { 
                            Text(
                                text = if (item.status == QueueStatus.FAILED) "Error: ${item.lastError}" 
                                       else "Created at ${dateFormat.format(Date(item.createdAt))}",
                                color = if (item.status == QueueStatus.FAILED) Color.Red else Color.Unspecified
                            ) 
                        },
                        trailingContent = {
                            Icon(
                                if (item.status == QueueStatus.FAILED) Icons.Default.SyncProblem else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (item.status == QueueStatus.FAILED) Color.Red else Color.Gray
                            )
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Queue is empty. Everything synced!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Note: The Offline Queue ensures that sales and adjustments performed while disconnected are reliably synchronized once a connection is restored.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SyncStat(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
    }
}
