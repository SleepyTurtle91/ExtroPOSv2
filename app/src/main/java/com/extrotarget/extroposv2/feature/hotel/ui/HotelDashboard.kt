package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.hotel.*
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDashboard(
    viewModel: HotelViewModel = hiltViewModel(),
    salesViewModel: SalesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val rooms by viewModel.rooms.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val occupancyRate by viewModel.occupancyRate.collectAsState()
    val salesUiState by salesViewModel.uiState.collectAsState()
    val activeMode = salesUiState.activeMode

    var showBookingDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRoomActionDialog by remember { mutableStateOf(false) }
    var selectedRoomForBooking by remember { mutableStateOf<Room?>(null) }
    var selectedRoomForAction by remember { mutableStateOf<Room?>(null) }
    var selectedBookingForAction by remember { mutableStateOf<Booking?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (activeMode == BusinessMode.HOTEL) "Hotel Management" else "Homestay Management")
                        Text(dateFormat.format(Date(selectedDate)), style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        },
        floatingActionButton = {
            if (activeMode == BusinessMode.HOMESTAY) {
                FloatingActionButton(onClick = { showBookingDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Booking")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // Summary row
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Occupancy",
                    value = "${occupancyRate.toInt()}%",
                    color = activeMode.color,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Active Bookings",
                    value = bookings.size.toString(),
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
            }

            if (activeMode.hasRoomManagement) {
                // Hotel Grid View
                Text(
                    "Rooms & Availability",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms) { room ->
                        val bookingForRoom = bookings.find { it.roomId == room.id }
                        RoomCard(
                            room = room,
                            booking = bookingForRoom,
                            onClick = {
                                if (room.status == RoomStatus.AVAILABLE) {
                                    selectedRoomForBooking = room
                                    showBookingDialog = true
                                } else {
                                    selectedRoomForAction = room
                                    selectedBookingForAction = bookingForRoom
                                    showRoomActionDialog = true
                                }
                            }
                        )
                    }
                }
            } else {
                // Homestay List View (Simplified)
                Text(
                    "Recent Bookings",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bookings) { booking ->
                        BookingListItem(booking)
                    }
                }
            }
        }
    }

    if (showBookingDialog) {
        BookingDialog(
            room = selectedRoomForBooking,
            onDismiss = { 
                showBookingDialog = false
                selectedRoomForBooking = null
            },
            onConfirm = { guest, checkIn, checkOut, amount ->
                viewModel.createBooking(
                    roomId = selectedRoomForBooking?.id ?: "HOMESTAY",
                    guest = guest,
                    checkIn = checkIn,
                    checkOut = checkOut,
                    totalAmount = amount
                )
                showBookingDialog = false
                selectedRoomForBooking = null
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.selectDate(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showRoomActionDialog && selectedRoomForAction != null) {
        RoomActionDialog(
            room = selectedRoomForAction!!,
            booking = selectedBookingForAction,
            onDismiss = {
                showRoomActionDialog = false
                selectedRoomForAction = null
                selectedBookingForAction = null
            },
            onCheckOut = {
                selectedBookingForAction?.let { viewModel.checkOut(it) }
                showRoomActionDialog = false
                selectedRoomForAction = null
                selectedBookingForAction = null
            },
            onUpdateStatus = { newStatus ->
                viewModel.updateRoomStatus(selectedRoomForAction!!, newStatus)
                showRoomActionDialog = false
                selectedRoomForAction = null
                selectedBookingForAction = null
            }
        )
    }
}

@Composable
fun RoomActionDialog(
    room: Room,
    booking: Booking?,
    onDismiss: () -> Unit,
    onCheckOut: () -> Unit,
    onUpdateStatus: (RoomStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Room ${room.name} Management", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (booking != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Current Guest Details", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            Text("Guest ID: ${booking.guestId.takeLast(6)}", fontWeight = FontWeight.Bold)
                            Text("Total Amount: ${CurrencyUtils.format(booking.totalAmount)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Text("Quick Actions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                if (room.status == RoomStatus.OCCUPIED || booking != null) {
                    Button(
                        onClick = onCheckOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CHECK-OUT GUEST")
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))
                Text("Update Room Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("DIRTY", Color(0xFF64748B), onClick = { onUpdateStatus(RoomStatus.DIRTY) })
                    StatusChip("CLEAN", Color(0xFF10B981), onClick = { onUpdateStatus(RoomStatus.AVAILABLE) })
                    StatusChip("MAINT", Color(0xFFF59E0B), onClick = { onUpdateStatus(RoomStatus.MAINTENANCE) })
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        }
    )
}

@Composable
fun StatusChip(label: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun RoomCard(
    room: Room,
    booking: Booking?,
    onClick: () -> Unit
) {
    val statusColor = when (room.status) {
        RoomStatus.AVAILABLE -> Color(0xFF10B981)
        RoomStatus.OCCUPIED -> Color(0xFFEF4444)
        RoomStatus.MAINTENANCE -> Color(0xFFF59E0B)
        RoomStatus.DIRTY -> Color(0xFF64748B)
        RoomStatus.RESERVED -> Color(0xFF3B82F6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(room.name, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(room.type, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        room.status.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (booking != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                    Text("Guest: ${booking.guestId.takeLast(4)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
            } else {
                Text(
                    CurrencyUtils.format(room.basePrice) + " / night",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(value, color = color, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun BookingListItem(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {
                    Icon(
                        Icons.Default.Hotel,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color(0xFF475569)
                    )
                }
                Column {
                    Text("Booking #${booking.id.takeLast(6)}", fontWeight = FontWeight.Bold)
                    Text("Check-in: ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(booking.checkInDate))}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyUtils.format(booking.totalAmount), fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Text(booking.status.name, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
            }
        }
    }
}

@Composable
fun BookingDialog(
    room: Room?,
    onDismiss: () -> Unit,
    onConfirm: (Guest, Long, Long, BigDecimal) -> Unit
) {
    var guestName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nights by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (room != null) "Booking for Room ${room.name}" else "New Homestay Booking", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Guest Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nights,
                    onValueChange = { nights = it },
                    label = { Text("Number of Nights") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val guest = Guest(id = UUID.randomUUID().toString(), name = guestName, phone = phone)
                    val total = (room?.basePrice ?: BigDecimal("100")).multiply(BigDecimal(nights.ifEmpty { "1" }))
                    onConfirm(guest, System.currentTimeMillis(), System.currentTimeMillis() + 86400000 * nights.toLong(), total)
                },
                enabled = guestName.isNotBlank()
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
