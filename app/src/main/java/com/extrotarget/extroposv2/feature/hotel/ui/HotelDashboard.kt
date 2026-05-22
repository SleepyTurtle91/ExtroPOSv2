package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.data.model.hotel.*
import com.extrotarget.extroposv2.feature.hotel.data.BookingFinancialSummary
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
    
    val showBookingWizard by viewModel.showBookingWizard.collectAsState()
    val showSettlementDialog by viewModel.showSettlementDialog.collectAsState()
    val financialSummary by viewModel.financialSummary.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(
                            if (activeMode == BusinessMode.HOTEL) R.string.hotel_dashboard_title 
                            else R.string.homestay_dashboard_title
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleBookingWizard(true) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.hotel_new_booking))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Picker Placeholder
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.hotel_current_date), style = MaterialTheme.typography.labelSmall)
                            Text(
                                dateFormat.format(Date(selectedDate)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { /* Open Date Picker */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.hotel_select_date))
                        }
                    }
                }
            }

            // Summary section
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activeMode.hasRoomManagement) {
                        SummaryCard(stringResource(R.string.hotel_occupancy_rate), "${occupancyRate.toInt()}%", Modifier.weight(1f))
                    }
                    SummaryCard(stringResource(R.string.hotel_active_bookings), bookings.size.toString(), Modifier.weight(1f))
                }
            }

            // Room List Header
            if (activeMode.hasRoomManagement) {
                item {
                    Text(stringResource(R.string.hotel_room_status), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(rooms) { room ->
                    RoomItem(
                        room = room,
                        onCheckIn = { 
                            bookings.find { it.roomId == room.id && it.status == BookingStatus.CONFIRMED }?.let { 
                                viewModel.checkIn(it) 
                            }
                        },
                        onCheckOut = { 
                            bookings.find { it.roomId == room.id && it.status == BookingStatus.CHECKED_IN }?.let { 
                                viewModel.startSettlement(it) 
                            }
                        }
                    )
                }
            } else {
                item {
                    Text(stringResource(R.string.hotel_arrivals_departures), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                // Simplified booking list for Homestay
                items(bookings) { booking ->
                    BookingListItem(
                        booking = booking,
                        onCheckIn = { viewModel.checkIn(booking) },
                        onCheckOut = { viewModel.startSettlement(booking) }
                    )
                }
            }
        }
    }

    if (showBookingWizard) {
        BookingWizardDialog(
            rooms = rooms,
            onDismiss = { viewModel.toggleBookingWizard(false) },
            onConfirm = { room, name, id, checkIn, checkOut, deposit ->
                viewModel.createBooking(room, name, id, checkIn, checkOut, deposit)
            }
        )
    }

    if (showSettlementDialog != null && financialSummary != null) {
        SettlementDialog(
            summary = financialSummary!!,
            onDismiss = { viewModel.dismissSettlement() },
            onProcessPayment = { amount ->
                salesViewModel.processHospitalityCheckout(
                    total = amount,
                    label = "Settlement: ${financialSummary!!.booking.id.takeLast(6)}",
                    onComplete = {
                        viewModel.checkOut(financialSummary!!.booking)
                        viewModel.dismissSettlement()
                    }
                )
            }
        )
    }
}

@Composable
fun BookingListItem(
    booking: Booking,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.hotel_booking_label, booking.id.takeLast(6)), fontWeight = FontWeight.Bold)
                Text(stringResource(booking.status.displayName), style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(booking.totalAmount.toString(), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(end = 16.dp))
                if (booking.status == BookingStatus.CONFIRMED) {
                    Button(onClick = onCheckIn, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.hotel_check_in), fontSize = 12.sp)
                    }
                } else if (booking.status == BookingStatus.CHECKED_IN) {
                    Button(onClick = onCheckOut, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.hotel_check_out), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomItem(
    room: Room,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(room.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(room.type, style = MaterialTheme.typography.bodySmall)
                if (!room.amenities.isNullOrBlank()) {
                    Text(room.amenities, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(
                    containerColor = when (room.status) {
                        RoomStatus.AVAILABLE -> Color(0xFF10B981) // Emerald 500
                        RoomStatus.OCCUPIED -> MaterialTheme.colorScheme.error
                        RoomStatus.RESERVED -> Color(0xFFF59E0B) // Amber 500
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(stringResource(room.status.displayName), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White)
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Room Actions")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.hotel_check_in)) },
                            onClick = { onCheckIn(); showMenu = false },
                            enabled = room.status == RoomStatus.AVAILABLE || room.status == RoomStatus.RESERVED
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.hotel_check_out)) },
                            onClick = { onCheckOut(); showMenu = false },
                            enabled = room.status == RoomStatus.OCCUPIED
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.hotel_mark_cleaned)) },
                            onClick = { showMenu = false },
                            enabled = room.status == RoomStatus.DIRTY
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.hotel_view_details)) },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingWizardDialog(
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onConfirm: (Room, String, String, Long, Long, BigDecimal) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var idNum by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var deposit by remember { mutableStateOf("0") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.hotel_new_booking), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.hotel_guest_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = idNum, onValueChange = { idNum = it }, label = { Text(stringResource(R.string.hotel_guest_id)) }, modifier = Modifier.fillMaxWidth())
                
                Text(stringResource(R.string.hotel_select_room), style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rooms.filter { it.status == RoomStatus.AVAILABLE }) { room ->
                        FilterChip(
                            selected = selectedRoom == room,
                            onClick = { selectedRoom = room },
                            label = { Text(room.name) }
                        )
                    }
                }

                OutlinedTextField(value = deposit, onValueChange = { deposit = it }, label = { Text(stringResource(R.string.hotel_collect_deposit)) }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                    Button(
                        onClick = { 
                            selectedRoom?.let { 
                                onConfirm(it, name, idNum, System.currentTimeMillis(), System.currentTimeMillis() + 86400000, deposit.toBigDecimalOrNull() ?: BigDecimal.ZERO) 
                            } 
                        },
                        enabled = name.isNotBlank() && selectedRoom != null
                    ) {
                        Text(stringResource(R.string.btn_confirm))
                    }
                }
            }
        }
    }
}

@Composable
fun SettlementDialog(
    summary: BookingFinancialSummary,
    onDismiss: () -> Unit,
    onProcessPayment: (BigDecimal) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.hotel_settlement), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.hotel_room_charge))
                    Text(summary.booking.totalAmount.toString())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.hotel_addons_total))
                    Text(summary.addonsTotal.toString())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.hotel_deposit_paid))
                    Text("-${summary.booking.depositAmount}", color = Color(0xFF10B981))
                }
                
                Divider()
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.hotel_balance_due), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(summary.balanceDue.toString(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = { onProcessPayment(summary.balanceDue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.hotel_final_payment))
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}
