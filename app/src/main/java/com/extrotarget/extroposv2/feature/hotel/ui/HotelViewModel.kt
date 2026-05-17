package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.hotel.*
import com.extrotarget.extroposv2.feature.hotel.data.HotelRepository
import com.extrotarget.extroposv2.feature.hotel.domain.usecase.GetOccupancyRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HotelViewModel @Inject constructor(
    private val repository: HotelRepository,
    private val getOccupancyRateUseCase: GetOccupancyRateUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val occupancyRate: StateFlow<Float> = selectedDate.flatMapLatest { date ->
        getOccupancyRateUseCase(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val rooms: StateFlow<List<Room>> = repository.getAllRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<Booking>> = selectedDate.flatMapLatest { date ->
        val start = getStartOfDay(date)
        val end = getEndOfDay(date)
        repository.getBookingsByDate(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun createBooking(roomId: String, guest: Guest, checkIn: Long, checkOut: Long, totalAmount: BigDecimal) {
        viewModelScope.launch {
            val booking = Booking(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                guestId = guest.id,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                totalAmount = totalAmount,
                status = BookingStatus.CONFIRMED
            )
            repository.createBooking(booking, guest)
            
            // Mark room as occupied
            val currentRooms = repository.getAllRooms().first()
            currentRooms.find { it.id == roomId }?.let { room ->
                repository.addRoom(room.copy(status = RoomStatus.OCCUPIED))
            }
        }
    }

    fun checkIn(booking: Booking) {
        viewModelScope.launch {
            repository.updateBookingStatus(booking, BookingStatus.CHECKED_IN)
        }
    }

    fun checkOut(booking: Booking) {
        viewModelScope.launch {
            repository.updateBookingStatus(booking, BookingStatus.CHECKED_OUT)
            // Also set room back to dirty
            val currentRooms = repository.getAllRooms().first()
            currentRooms.find { it.id == booking.roomId }?.let { room ->
                repository.addRoom(room.copy(status = RoomStatus.DIRTY))
            }
        }
    }

    fun updateRoomStatus(room: Room, status: RoomStatus) {
        viewModelScope.launch {
            repository.addRoom(room.copy(status = status))
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
