package com.extrotarget.extroposv2.core.domain.commerce

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CommerceEvent {
    data class TransactionCompleted(val transactionId: String) : CommerceEvent
    data class StockAlert(val productId: String, val currentStock: java.math.BigDecimal) : CommerceEvent
    data class FnbOrderCreated(val orderId: String) : CommerceEvent
    data class FnbOrderStatusChanged(val orderId: String, val newStatus: WorkflowStatus) : CommerceEvent
}

@Singleton
class CommerceEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<CommerceEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    suspend fun emit(event: CommerceEvent) {
        _events.emit(event)
    }
}
