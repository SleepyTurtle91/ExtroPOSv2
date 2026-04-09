package com.extrotarget.extroposv2.ui.settings.payment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.local.dao.settings.PaymentMethodDao
import com.extrotarget.extroposv2.core.data.model.settings.PaymentMethod
import com.extrotarget.extroposv2.core.data.model.settings.PaymentMethodType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PaymentMethodUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao
) : ViewModel() {

    val uiState: StateFlow<PaymentMethodUiState> = paymentMethodDao.getAllPaymentMethods()
        .map { PaymentMethodUiState(paymentMethods = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentMethodUiState(isLoading = true))

    init {
        viewModelScope.launch {
            if (paymentMethodDao.getCount() == 0) {
                // Initialize default methods
                paymentMethodDao.insertPaymentMethod(PaymentMethod("CASH", "Cash", PaymentMethodType.CASH))
                paymentMethodDao.insertPaymentMethod(PaymentMethod("CARD", "Card", PaymentMethodType.CARD))
                paymentMethodDao.insertPaymentMethod(PaymentMethod("DUITNOW", "DuitNow QR", PaymentMethodType.CUSTOM))
            }
        }
    }

    fun addCustomPaymentMethod(name: String) {
        viewModelScope.launch {
            val method = PaymentMethod(
                id = UUID.randomUUID().toString(),
                name = name,
                type = PaymentMethodType.CUSTOM
            )
            paymentMethodDao.insertPaymentMethod(method)
        }
    }

    fun togglePaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            paymentMethodDao.updatePaymentMethod(method.copy(isEnabled = !method.isEnabled))
        }
    }

    fun deletePaymentMethod(method: PaymentMethod) {
        if (method.type == PaymentMethodType.CUSTOM) {
            viewModelScope.launch {
                paymentMethodDao.deletePaymentMethod(method)
            }
        }
    }
}
