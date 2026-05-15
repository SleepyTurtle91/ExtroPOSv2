package com.extrotarget.extroposv2.feature.hotel.data

import com.extrotarget.extroposv2.core.data.local.dao.hotel.HotelDao
import com.extrotarget.extroposv2.core.data.model.hotel.*
import kotlinx.coroutines.flow.Flow
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
    }

    suspend fun updateBookingStatus(booking: Booking, status: BookingStatus) {
        hotelDao.updateBooking(booking.copy(status = status))
    }

    fun getAddons(bookingId: String): Flow<List<HotelAddon>> = 
        hotelDao.getAddonsForBooking(bookingId)

    suspend fun addAddon(addon: HotelAddon) = hotelDao.insertAddon(addon)
}
