package com.extrotarget.extroposv2.ui.sales

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.extrotarget.extroposv2.R

enum class BusinessMode(
    val id: String,
    @StringRes val displayName: Int,
    @StringRes val description: Int,
    val icon: ImageVector,
    val color: Color,
    val hasTables: Boolean = false,
    val hasStaffAssignment: Boolean = false,
    val hasWeightSupport: Boolean = false,
    val hasBookings: Boolean = false,
    val hasRoomManagement: Boolean = false,
) {
    RETAIL(
        id = "retail",
        displayName = R.string.mode_retail_name,
        description = R.string.mode_retail_desc,
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFF3B82F6) // Blue 500
    ),
    FNB(
        id = "fnb",
        displayName = R.string.mode_fnb_name,
        description = R.string.mode_fnb_desc,
        icon = Icons.Default.Restaurant,
        color = Color(0xFFF97316), // Orange 500
        hasTables = true
    ),
    CARWASH(
        id = "carwash",
        displayName = R.string.mode_carwash_name,
        description = R.string.mode_carwash_desc,
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFF10B981), // Emerald 500
        hasStaffAssignment = true
    ),
    LAUNDRY(
        id = "laundry",
        displayName = R.string.mode_laundry_name,
        description = R.string.mode_laundry_desc,
        icon = Icons.Default.LocalLaundryService,
        color = Color(0xFF6366F1), // Indigo 500
        hasWeightSupport = true
    ),
    HOTEL(
        id = "hotel",
        displayName = R.string.mode_hotel_name,
        description = R.string.mode_hotel_desc,
        icon = Icons.Default.Hotel,
        color = Color(0xFF8B5CF6), // Violet 500
        hasBookings = true,
        hasRoomManagement = true
    ),
    HOMESTAY(
        id = "homestay",
        displayName = R.string.mode_homestay_name,
        description = R.string.mode_homestay_desc,
        icon = Icons.Default.Home,
        color = Color(0xFFEC4899), // Pink 500
        hasBookings = true,
        hasRoomManagement = false
    ),
    KIOSK(
        id = "kiosk",
        displayName = R.string.mode_kiosk_name,
        description = R.string.mode_kiosk_desc,
        icon = Icons.Default.Monitor,
        color = Color(0xFF1E293B) // Slate 800
    )
}
