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

import com.extrotarget.extroposv2.core.data.model.platform.Capability
import com.extrotarget.extroposv2.core.platform.models.DashboardActionId

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
    val defaultCapabilities: Set<Capability> = emptySet(),
    val defaultActions: List<DashboardActionId> = emptyList()
) {
    RETAIL(
        id = "retail",
        displayName = R.string.mode_retail_name,
        description = R.string.mode_retail_desc,
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFF3B82F6), // Blue 500
        defaultCapabilities = setOf(Capability.LOYALTY_SYSTEM, Capability.DUITNOW_DYNAMIC_QR),
        defaultActions = listOf(DashboardActionId.NEW_SALE, DashboardActionId.INVENTORY_CHECK)
    ),
    FNB(
        id = "fnb",
        displayName = R.string.mode_fnb_name,
        description = R.string.mode_fnb_desc,
        icon = Icons.Default.Restaurant,
        color = Color(0xFFF97316), // Orange 500
        hasTables = true,
        defaultCapabilities = setOf(Capability.TABLE_MANAGEMENT, Capability.KITCHEN_DISPLAY, Capability.DUITNOW_DYNAMIC_QR),
        defaultActions = listOf(DashboardActionId.TABLE_ORDER, DashboardActionId.KITCHEN_QUEUE)
    ),
    CARWASH(
        id = "carwash",
        displayName = R.string.mode_carwash_name,
        description = R.string.mode_carwash_desc,
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFF10B981), // Emerald 500
        hasStaffAssignment = true,
        defaultCapabilities = setOf(Capability.STAFF_COMMISSION, Capability.DUITNOW_DYNAMIC_QR),
        defaultActions = listOf(DashboardActionId.NEW_CARWASH_JOB, DashboardActionId.CARWASH_QUEUE)
    ),
    LAUNDRY(
        id = "laundry",
        displayName = R.string.mode_laundry_name,
        description = R.string.mode_laundry_desc,
        icon = Icons.Default.LocalLaundryService,
        color = Color(0xFF6366F1), // Indigo 500
        hasWeightSupport = true,
        defaultCapabilities = setOf(Capability.WEIGHT_BASED_PRICING, Capability.DUITNOW_DYNAMIC_QR),
        defaultActions = listOf(DashboardActionId.RECEIVE_LAUNDRY, DashboardActionId.LAUNDRY_PICKUP)
    ),
    HOTEL(
        id = "hotel",
        displayName = R.string.mode_hotel_name,
        description = R.string.mode_hotel_desc,
        icon = Icons.Default.Hotel,
        color = Color(0xFF8B5CF6), // Violet 500
        hasBookings = true,
        hasRoomManagement = true,
        defaultCapabilities = setOf(Capability.BOOKING_MANAGEMENT, Capability.ROOM_MANAGEMENT),
        defaultActions = listOf(DashboardActionId.ROOM_BOOKING)
    ),
    HOMESTAY(
        id = "homestay",
        displayName = R.string.mode_homestay_name,
        description = R.string.mode_homestay_desc,
        icon = Icons.Default.Home,
        color = Color(0xFFEC4899), // Pink 500
        hasBookings = true,
        hasRoomManagement = false,
        defaultCapabilities = setOf(Capability.BOOKING_MANAGEMENT),
        defaultActions = listOf(DashboardActionId.ROOM_BOOKING)
    ),
    KIOSK(
        id = "kiosk",
        displayName = R.string.mode_kiosk_name,
        description = R.string.mode_kiosk_desc,
        icon = Icons.Default.Monitor,
        color = Color(0xFF1E293B), // Slate 800
        defaultCapabilities = setOf(Capability.KIOSK_SELF_SERVICE, Capability.DUITNOW_DYNAMIC_QR),
        defaultActions = listOf(DashboardActionId.NEW_SALE)
    )
}
