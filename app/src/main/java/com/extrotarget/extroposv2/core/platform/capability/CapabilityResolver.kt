package com.extrotarget.extroposv2.core.platform.capability

import com.extrotarget.extroposv2.core.data.model.platform.Capability
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityResolver @Inject constructor() {

    fun getCapabilitiesForMode(mode: BusinessMode): Set<Capability> {
        return when (mode) {
            BusinessMode.RETAIL -> setOf(
                Capability.INVENTORY,
                Capability.BARCODE_SCANNING,
                Capability.LOYALTY_SYSTEM,
                Capability.LHDN_E_INVOICING,
                Capability.DUITNOW_DYNAMIC_QR
            )
            BusinessMode.FNB -> setOf(
                Capability.INVENTORY,
                Capability.TABLE_MANAGEMENT,
                Capability.KITCHEN_DISPLAY,
                Capability.KITCHEN_TICKET,
                Capability.MODIFIER_SYSTEM,
                Capability.COMBO_MENU,
                Capability.LOYALTY_SYSTEM,
                Capability.LHDN_E_INVOICING,
                Capability.DUITNOW_DYNAMIC_QR
            )
            BusinessMode.CARWASH -> setOf(
                Capability.STAFF_COMMISSION,
                Capability.LOYALTY_SYSTEM,
                Capability.DUITNOW_DYNAMIC_QR
            )
            BusinessMode.LAUNDRY -> setOf(
                Capability.WEIGHT_BASED_PRICING,
                Capability.LOYALTY_SYSTEM,
                Capability.DUITNOW_DYNAMIC_QR
            )
            BusinessMode.HOTEL, BusinessMode.HOMESTAY -> setOf(
                Capability.BOOKING_MANAGEMENT,
                Capability.ROOM_MANAGEMENT,
                Capability.LHDN_E_INVOICING
            )
            BusinessMode.KIOSK -> setOf(
                Capability.KIOSK_SELF_SERVICE,
                Capability.DUITNOW_DYNAMIC_QR
            )
        }
    }
}
