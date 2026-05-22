package com.extrotarget.extroposv2.feature.hotel.data

import com.extrotarget.extroposv2.core.data.local.dao.hotel.HotelDao
import com.extrotarget.extroposv2.core.data.model.hotel.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotelRepository @Inject constructor(
    private val hotelDao: HotelDao
) {
    fun getAllRooms(): Flow<List<Room>> = hotelDao.getAllRooms()

    suspend fun addRoom(room: Room) = hotelDao.insertRoom(room)

    fun getBookingsByDate(start: Long, end: Long): Flow<List<Booking>> = 
        hotelDao.getBookingsByDate(start, end)

    suspend fun createBooking(booking: Booking, guest: Guest) {
        hotelDao.insertGuest(guest)
        hotelDao.insertBooking(booking)
        
        // Update room status to RESERVED
        val room = hotelDao.getAllRooms().first().find { it.id == booking.roomId }
        if (room != null && room.status == RoomStatus.AVAILABLE) {
            hotelDao.updateRoom(room.copy(status = RoomStatus.RESERVED))
        }
    }

    suspend fun updateBookingStatus(booking: Booking, status: BookingStatus) {
        hotelDao.updateBooking(booking.copy(status = status))
        
        // Handle room status side effects
        val room = hotelDao.getAllRooms().first().find { it.id == booking.roomId } ?: return
        when (status) {
            BookingStatus.CHECKED_IN -> hotelDao.updateRoom(room.copy(status = RoomStatus.OCCUPIED))
            BookingStatus.CHECKED_OUT -> hotelDao.updateRoom(room.copy(status = RoomStatus.DIRTY))
            BookingStatus.CANCELLED -> hotelDao.updateRoom(room.copy(status = RoomStatus.AVAILABLE))
            else -> {}
        }
    }

    suspend fun getBookingFinancialSummary(bookingId: String): BookingFinancialSummary {
        val bookings = hotelDao.getBookingsByDate(0, Long.MAX_VALUE).first()
        val booking = bookings.find { it.id == bookingId } ?: throw Exception("Booking not found")
        val addons = hotelDao.getAddonsForBooking(bookingId).first()
        
        val addonsTotal = addons.fold(BigDecimal.ZERO) { acc, addon -> acc.add(addon.totalAmount) }
        val balanceDue = booking.totalAmount.add(addonsTotal).subtract(booking.depositAmount)
        
        return BookingFinancialSummary(
            booking = booking,
            addons = addons,
            addonsTotal = addonsTotal,
            balanceDue = balanceDue
        )
    }

    fun getAddons(bookingId: String): Flow<List<HotelAddon>> = 
        hotelDao.getAddonsForBooking(bookingId)

    suspend fun addAddon(addon: HotelAddon) = hotelDao.insertAddon(addon)
}

data class BookingFinancialSummary(
    val booking: Booking,
    val addons: List<HotelAddon>,
    val addonsTotal: BigDecimal,
    val balanceDue: BigDecimal
)
