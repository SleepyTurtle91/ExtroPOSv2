package com.extrotarget.extroposv2.core.data.model.platform

enum class Capability(
    val id: String,
    val displayName: String
) {
    TABLE_MANAGEMENT("table_management", "Table Floor Plan"),
    KITCHEN_DISPLAY("kds", "Kitchen Display System"),
    STAFF_COMMISSION("staff_commission", "Staff Performance & Earnings"),
    WEIGHT_BASED_PRICING("weight_pricing", "Digital Scale Integration"),
    BOOKING_MANAGEMENT("bookings", "Reservation & Booking System"),
    ROOM_MANAGEMENT("rooms", "Hotel Room Management"),
    KIOSK_SELF_SERVICE("kiosk", "Self-Service Terminal"),
    LHDN_E_INVOICING("e_invoicing", "LHDN MyInvois Integration"),
    LOYALTY_SYSTEM("loyalty", "Customer Points & Rewards"),
    DUITNOW_DYNAMIC_QR("duitnow_qr", "DuitNow Dynamic QR Payments")
}
