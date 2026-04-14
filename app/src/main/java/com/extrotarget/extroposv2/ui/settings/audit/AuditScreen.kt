package com.extrotarget.extroposv2.ui.settings.audit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.AuditLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    viewModel: AuditViewModel = hiltViewModel()
) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val modules by viewModel.modules.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val selectedModule by viewModel.selectedModule.collectAsState()
    val selectedStaffId by viewModel.selectedStaffId.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    var showModuleFilter by remember { mutableStateOf(false) }
    var showStaffFilter by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security & Audit Logs") },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date Filter")
                    }
                    Box {
                        IconButton(onClick = { showModuleFilter = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Module Filter")
                        }
                        DropdownMenu(expanded = showModuleFilter, onDismissRequest = { showModuleFilter = false }) {
                            DropdownMenuItem(text = { Text("All Modules") }, onClick = { viewModel.filterByModule(null); showModuleFilter = false })
                            modules.forEach { module ->
                                DropdownMenuItem(text = { Text(module) }, onClick = { viewModel.filterByModule(module); showModuleFilter = false })
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { showStaffFilter = true }) {
                            Icon(Icons.Default.Person, contentDescription = "Staff Filter")
                        }
                        DropdownMenu(expanded = showStaffFilter, onDismissRequest = { showStaffFilter = false }) {
                            DropdownMenuItem(text = { Text("All Staff") }, onClick = { viewModel.filterByStaff(null); showStaffFilter = false })
                            staffMembers.forEach { staff ->
                                DropdownMenuItem(text = { Text(staff.staffName) }, onClick = { viewModel.filterByStaff(staff.staffId); showStaffFilter = false })
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filter Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Period: ${dateFormat.format(Date(dateRange.first))} - ${dateFormat.format(Date(dateRange.second))}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedModule?.let { module ->
                            FilterChip(module, { viewModel.filterByModule(null) })
                        }
                        selectedStaffId?.let { staffId ->
                            val staffName = staffMembers.find { it.staffId == staffId }?.staffName ?: staffId
                            FilterChip(staffName, { viewModel.filterByStaff(null) })
                        }
                    }
                }
            }

            if (auditLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs found for selected filters")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs) { log ->
                        AuditLogItem(log, dateFormat, timeFormat)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedStartDateMillis?.let { start ->
                        datePickerState.selectedEndDateMillis?.let { end ->
                            viewModel.setDateRange(start, end)
                        }
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = datePickerState, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun FilterChip(text: String, onRemove: () -> Unit) {
    AssistChip(
        onClick = onRemove,
        label = { Text(text) },
        trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
    )
}

@Composable
fun AuditLogItem(log: AuditLog, dateFormat: SimpleDateFormat, timeFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(log.severity) {
                "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                "WARNING" -> Color(0xFFFFF9C4) // Yellow
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(log.module, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(log.staffName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(dateFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall)
                    Text(timeFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Text(log.action, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (log.details.isNotEmpty()) {
                Text(
                    log.details, 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
