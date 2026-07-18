package com.extrotarget.extroposv2.ui.settings.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.settings.diagnostics.viewmodel.DiagnosticsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SYSTEM DIAGNOSTICS", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }, enabled = !uiState.isRunning) {
                        if (uiState.isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Device Identity Section
            item {
                SectionHeader("DEVICE IDENTITY")
                uiState.identity?.let { identity ->
                    InfoCard {
                        InfoRow("Terminal Name", identity.terminalName)
                        InfoRow("Branch ID", identity.branchId)
                        InfoRow("Device ID", identity.deviceId)
                        InfoRow("Installation ID", identity.installationId)
                        InfoRow("Registered At", SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(identity.registeredAt)))
                    }
                }
            }

            // 2. Application Health Section
            item {
                SectionHeader("APPLICATION HEALTH")
                uiState.report?.let { report ->
                    InfoCard {
                        StatusRow(
                            label = "Database Integrity",
                            status = if (report.isDatabaseHealthy) "HEALTHY" else "CORRUPTED",
                            color = if (report.isDatabaseHealthy) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                        InfoRow("Last Diagnostic", SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(report.timestamp)))
                    }
                }
            }

            // 3. Storage Section
            item {
                SectionHeader("STORAGE USAGE")
                uiState.report?.storageInfo?.let { storage ->
                    InfoCard {
                        InfoRow("Database Size", formatBytes(storage.databaseSize))
                        InfoRow("Free Space", formatBytes(storage.freeSpace))
                        InfoRow("Total Storage", formatBytes(storage.totalSpace))
                        
                        LinearProgressIndicator(
                            progress = { (storage.totalSpace - storage.freeSpace).toFloat() / storage.totalSpace.toFloat() },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            color = Color(0xFF3B82F6),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StatusRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = status,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
