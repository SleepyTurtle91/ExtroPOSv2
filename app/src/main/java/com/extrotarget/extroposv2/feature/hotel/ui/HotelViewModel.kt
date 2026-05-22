package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.hotel.*
import com.extrotarget.extroposv2.feature.hotel.data.BookingFinancialSummary
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

    private val _showBookingWizard = MutableStateFlow(false)
    val showBookingWizard: StateFlow<Boolean> = _showBookingWizard.asStateFlow()

    private val _showSettlementDialog = MutableStateFlow<Booking?>(null)
    val showSettlementDialog: StateFlow<Booking?> = _showSettlementDialog.asStateFlow()

    private val _financialSummary = MutableStateFlow<BookingFinancialSummary?>(null)
    val financialSummary: StateFlow<BookingFinancialSummary?> = _financialSummary.asStateFlow()

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

    fun toggleBookingWizard(show: Boolean) {
        _showBookingWizard.value = show
    }

    fun startSettlement(booking: Booking) {
        viewModelScope.launch {
            val summary = repository.getBookingFinancialSummary(booking.id)
            _financialSummary.value = summary
            _showSettlementDialog.value = booking
        }
    }

    fun dismissSettlement() {
        _showSettlementDialog.value = null
        _financialSummary.value = null
    }

    fun createBooking(room: Room, guestName: String, guestId: String, checkIn: Long, checkOut: Long, deposit: BigDecimal) {
        viewModelScope.launch {
            val guest = Guest(
                id = UUID.randomUUID().toString(),
                name = guestName,
                idNumber = guestId
            )
            val nights = ((checkOut - checkIn) / (24 * 60 * 60 * 1000)).coerceAtLeast(1).toBigDecimal()
            val total = room.basePrice.multiply(nights)
            
            val booking = Booking(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                guestId = guest.id,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                totalAmount = total,
                depositAmount = deposit,
                status = BookingStatus.CONFIRMED
            )
            repository.createBooking(booking, guest)
            _showBookingWizard.value = false
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
