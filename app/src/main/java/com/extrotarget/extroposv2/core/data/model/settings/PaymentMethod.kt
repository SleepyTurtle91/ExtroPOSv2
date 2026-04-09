package com.extrotarget.extroposv2.core.data.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_methods")
data class PaymentMethod(
    @PrimaryKey val id: String,
    val name: String,
    val type: PaymentMethodType = PaymentMethodType.CUSTOM,
    val isEnabled: Boolean = true,
    val iconResId: Int? = null // Optional icon resource
)

enum class PaymentMethodType {
    CASH,
    CARD,
    E_WALLET, // For integrated ones
    CUSTOM    // For user-defined ones (e.g., "ShopeePay Manual")
}
