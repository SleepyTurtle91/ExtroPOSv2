package com.extrotarget.extroposv2.feature.hotel.domain.usecase

import com.extrotarget.extroposv2.feature.hotel.data.HotelRepository
import com.extrotarget.extroposv2.core.data.model.hotel.BookingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetOccupancyRateUseCase @Inject constructor(
    private val repository: HotelRepository
) {
    /**
     * Calculates the occupancy rate as a percentage.
     * Formula: (Occupied Rooms / Total Rooms) * 100
     */
    operator fun invoke(date: Long): Flow<Float> {
        val start = getStartOfDay(date)
        val end = getEndOfDay(date)
        
        return combine(
            repository.getAllRooms(),
            repository.getBookingsByDate(start, end)
        ) { rooms, bookings ->
            if (rooms.isEmpty()) return@combine 0f
            
            val occupiedCount = bookings.count { 
                it.status == BookingStatus.CHECKED_IN || it.status == BookingStatus.CONFIRMED 
            }
            
            (occupiedCount.toFloat() / rooms.size.toFloat()) * 100f
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        return java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
