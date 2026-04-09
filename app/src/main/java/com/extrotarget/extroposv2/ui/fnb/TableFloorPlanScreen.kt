package com.extrotarget.extroposv2.ui.fnb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import com.extrotarget.extroposv2.ui.fnb.viewmodel.TableViewModel

import com.extrotarget.extroposv2.ui.fnb.components.ZoneSelector

@Composable
fun TableFloorPlanScreen(
    viewModel: TableViewModel,
    onTableClick: (Table) -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val selectedZone by viewModel.selectedZone.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                Text(
                    text = "Floor Plan",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                ZoneSelector(
                    zones = zones,
                    selectedZone = selectedZone,
                    onZoneSelected = { viewModel.selectZone(it) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Table")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tables) { table ->
                    TableCard(
                        table = table,
                        onClick = { onTableClick(table) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTableDialog(
            currentZone = selectedZone,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cap, zone ->
                viewModel.addTable(name, cap, zone)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableCard(
    table: Table,
    onClick: () -> Unit
) {
    val backgroundColor = when (table.status) {
        TableStatus.AVAILABLE -> Color(0xFF4CAF50) // Green
        TableStatus.OCCUPIED -> Color(0xFFF44336) // Red
        TableStatus.RESERVED -> Color(0xFFFF9800) // Orange
        TableStatus.DIRTY -> Color(0xFF795548)    // Brown
    }

    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = table.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${table.capacity} Pax",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = table.status.name,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun AddTableDialog(
    currentZone: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("4") }
    var zone by remember { mutableStateOf(currentZone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Table") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Table Name (e.g. Table 1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity (Pax)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = zone,
                    onValueChange = { zone = it },
                    label = { Text("Zone (e.g. Indoor, Outdoor)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, capacity.toIntOrNull() ?: 4, zone) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
