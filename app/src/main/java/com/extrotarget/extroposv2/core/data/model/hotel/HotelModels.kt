package com.extrotarget.extroposv2.core.data.model.hotel

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "hotel_rooms")
data class Room(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // e.g., "Single", "Double", "Deluxe", "Homestay"
    val basePrice: BigDecimal,
    val amenities: String? = null, // Comma-separated
    val status: RoomStatus = RoomStatus.AVAILABLE,
    val imageUrl: String? = null
)

enum class RoomStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE,
    DIRTY,
    RESERVED
}

@Entity(tableName = "hotel_guests")
data class Guest(
    @PrimaryKey val id: String,
    val name: String,
    val idNumber: String? = null, // Passport/IC
    val phone: String? = null,
    val email: String? = null,
    val nationality: String? = null,
    val loyaltyTier: String = "REGULAR"
)

@Entity(
    tableName = "hotel_bookings",
    foreignKeys = [
        ForeignKey(
            entity = Room::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Guest::class,
            parentColumns = ["id"],
            childColumns = ["guestId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("roomId"), Index("guestId")]
)
data class Booking(
    @PrimaryKey val id: String,
    val roomId: String,
    val guestId: String,
    val checkInDate: Long, // Timestamp
    val checkOutDate: Long, // Timestamp
    val totalAmount: BigDecimal,
    val depositAmount: BigDecimal = BigDecimal.ZERO,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val note: String? = null,
    val staffId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class BookingStatus {
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED,
    NO_SHOW
}

@Entity(
    tableName = "hotel_addons",
    foreignKeys = [
        ForeignKey(
            entity = Booking::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookingId")]
)
data class HotelAddon(
    @PrimaryKey val id: String,
    val bookingId: String,
    val name: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalAmount: BigDecimal,
    val type: String // e.g., "MEAL", "TOUR", "TRANSPORT"
)
