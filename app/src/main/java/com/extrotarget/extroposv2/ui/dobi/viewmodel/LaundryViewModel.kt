package com.extrotarget.extroposv2.ui.dobi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryOrder
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryStatus
import com.extrotarget.extroposv2.core.data.repository.dobi.LaundryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.extrotarget.extroposv2.core.hardware.scale.ScaleInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

data class LaundryUiState(
    val orders: List<LaundryOrder> = emptyList(),
    val isLoading: Boolean = false,
    val pricePerKg: BigDecimal = BigDecimal("3.50"), // Standard MYR rate
    val liveWeight: BigDecimal = BigDecimal.ZERO
)

@HiltViewModel
class LaundryViewModel @Inject constructor(
    private val laundryRepository: LaundryRepository,
    private val scale: ScaleInterface,
    private val whatsAppManager: com.extrotarget.extroposv2.core.util.notification.WhatsAppManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaundryUiState())
    val uiState: StateFlow<LaundryUiState> = combine(
        _uiState,
        scale.getWeightFlow()
    ) { state, weight ->
        state.copy(liveWeight = weight)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LaundryUiState())

    init {
        viewModelScope.launch {
            scale.connect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            scale.disconnect()
        }
    }

    fun tareScale() {
        viewModelScope.launch {
            scale.tare()
        }
    }

    val orders: StateFlow<List<LaundryOrder>> = laundryRepository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createOrder(name: String, phone: String, weight: BigDecimal, note: String?) {
        viewModelScope.launch {
            val totalPrice = weight.multiply(_uiState.value.pricePerKg)
            val newOrder = LaundryOrder(
                id = UUID.randomUUID().toString(),
                customerName = name,
                customerPhone = phone,
                weightKg = weight,
                totalPrice = totalPrice,
                note = note
            )
            laundryRepository.createOrder(newOrder)
        }
    }

    fun updateStatus(order: LaundryOrder, status: LaundryStatus) {
        viewModelScope.launch {
            when (status) {
                LaundryStatus.READY -> {
                    laundryRepository.markAsReady(order.id)
                    whatsAppManager.sendOrderReadyNotification(order.copy(status = LaundryStatus.READY))
                }
                LaundryStatus.COLLECTED -> laundryRepository.markAsCollected(order.id)
                else -> {
                    // Manual update for other statuses
                }
            }
        }
    }
}