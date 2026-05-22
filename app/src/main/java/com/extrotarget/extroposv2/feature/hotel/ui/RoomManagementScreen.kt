package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.extrotarget.extroposv2.core.data.model.hotel.Room
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoomManagementViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRoom by remember { mutableStateOf<Room?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Configuration", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Room")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Icon(
                                Icons.Default.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = Color(0xFF3B82F6)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(room.name, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(room.type, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                            Text(
                                CurrencyUtils.format(room.basePrice) + " / night",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { editingRoom = room }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingRoom != null) {
        AddEditRoomDialog(
            room = editingRoom,
            onDismiss = {
                showAddDialog = false
                editingRoom = null
            },
            onConfirm = { name, type, price ->
                if (editingRoom == null) {
                    viewModel.addRoom(name, type, price)
                } else {
                    viewModel.updateRoom(editingRoom!!.copy(name = name, type = type, basePrice = price))
                }
                showAddDialog = false
                editingRoom = null
            }
        )
    }
}

@Composable
fun AddEditRoomDialog(
    room: Room?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, BigDecimal) -> Unit
) {
    var name by remember { mutableStateOf(room?.name ?: "") }
    var type by remember { mutableStateOf(room?.type ?: "Deluxe") }
    var price by remember { mutableStateOf(room?.basePrice?.toString() ?: "") }

    val roomTypes = listOf("Single", "Double", "Deluxe", "Suite", "Homestay Unit")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (room == null) "Add New Room" else "Edit Room", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Room Name/Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Simple Type Selection
                Text("Room Type", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    roomTypes.take(3).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 10.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    roomTypes.drop(3).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Base Price / Night (RM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, type, price.toBigDecimalOrNull() ?: BigDecimal.ZERO)
                },
                enabled = name.isNotBlank() && price.isNotBlank()
            ) {
                Text("SAVE ROOM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
