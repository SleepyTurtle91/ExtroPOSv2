package com.extrotarget.extroposv2.core.data.local.dao.hotel

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.hotel.Room
import com.extrotarget.extroposv2.core.data.model.hotel.Booking
import com.extrotarget.extroposv2.core.data.model.hotel.Guest
import com.extrotarget.extroposv2.core.data.model.hotel.HotelAddon
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface HotelDao {

    // Room Management
    @Query("SELECT * FROM hotel_rooms")
    fun getAllRooms(): Flow<List<Room>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: Room)

    @Update
    suspend fun updateRoom(room: Room)

    // Booking Management
    @Query("SELECT * FROM hotel_bookings WHERE roomId = :roomId AND ((checkInDate < :end AND checkOutDate > :start)) AND status != 'CANCELLED'")
    fun getBookingsForRoomInRange(roomId: String, start: Long, end: Long): Flow<List<Booking>>

    @Query("SELECT * FROM hotel_bookings WHERE checkInDate BETWEEN :start AND :end")
    fun getBookingsByDate(start: Long, end: Long): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    // Guest Management
    @Query("SELECT * FROM hotel_guests WHERE id = :guestId")
    suspend fun getGuestById(guestId: String): Guest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: Guest)

    // Addon Management
    @Query("SELECT * FROM hotel_addons WHERE bookingId = :bookingId")
    fun getAddonsForBooking(bookingId: String): Flow<List<HotelAddon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddon(addon: HotelAddon)

    // Reporting Queries
    @Query("""
        SELECT 
            totalAmount as revenue,
            status
        FROM hotel_bookings
        WHERE timestamp BETWEEN :start AND :end AND status IN ('CHECKED_OUT', 'CHECKED_IN', 'CONFIRMED')
    """)
    fun getRawHotelRevenueSummary(start: Long, end: Long): Flow<List<RawHotelRevenue>>
}

data class RawHotelRevenue(
    val revenue: BigDecimal,
    val status: String
)
