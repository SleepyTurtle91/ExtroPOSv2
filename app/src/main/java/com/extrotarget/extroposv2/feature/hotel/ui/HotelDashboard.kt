package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.hotel.*
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.viewmodel.SalesViewModel
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

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (activeMode == BusinessMode.HOTEL) "Hotel Management" else "Homestay Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Back Icon
                    }
                }
            )
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
                            Text("Current Date", style = MaterialTheme.typography.labelSmall)
                            Text(
                                dateFormat.format(Date(selectedDate)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { /* Open Date Picker */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                }
            }

            // Summary section
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activeMode.hasRoomManagement) {
                        SummaryCard("Occupancy Rate", "${occupancyRate.toInt()}%", Modifier.weight(1f))
                    }
                    SummaryCard("Active Bookings", bookings.size.toString(), Modifier.weight(1f))
                }
            }

            // Room List Header
            if (activeMode.hasRoomManagement) {
                item {
                    Text("Room Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(rooms) { room ->
                    RoomItem(room)
                }
            } else {
                item {
                    Text("Today's Arrivals/Departures", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                // Simplified booking list for Homestay
                items(bookings) { booking ->
                    BookingListItem(booking)
                }
            }
        }
    }
}

@Composable
fun BookingListItem(booking: Booking) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Booking #${booking.id.takeLast(6)}", fontWeight = FontWeight.Bold)
                Text(booking.status.name, style = MaterialTheme.typography.bodySmall)
            }
            Text(booking.totalAmount.toString(), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun RoomItem(room: Room) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(room.name, fontWeight = FontWeight.Bold)
                Text(room.type, style = MaterialTheme.typography.bodySmall)
            }
            Badge(
                containerColor = when (room.status) {
                    RoomStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
                    RoomStatus.OCCUPIED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(room.status.name, modifier = Modifier.padding(4.dp))
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
