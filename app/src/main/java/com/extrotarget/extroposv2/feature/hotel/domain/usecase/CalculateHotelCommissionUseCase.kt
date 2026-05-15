package com.extrotarget.extroposv2.feature.hotel.domain.usecase

import com.extrotarget.extroposv2.core.data.model.hotel.Booking
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateHotelCommissionUseCase @Inject constructor() {
    
    /**
     * Calculates commission for a booking.
     * @param booking The booking to calculate for.
     * @param rate The commission rate as a percentage (e.g., 10.0 for 10%).
     * @return The calculated commission as a BigDecimal.
     */
    fun execute(booking: Booking, rate: Double): BigDecimal {
        val bookingAmount = booking.totalAmount
        val commissionRate = BigDecimal.valueOf(rate).divide(BigDecimal.valueOf(100))
        
        return bookingAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_EVEN)
    }
}
