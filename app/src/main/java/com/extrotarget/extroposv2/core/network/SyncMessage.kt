package com.extrotarget.extroposv2.core.network

import java.math.BigDecimal

/**
 * Structured message types for P2P synchronization to avoid hardcoded strings.
 */
object SyncMessageType {
    const val PUSH_SALE = "PUSH_SALE"
    const val SALE_COMPLETED = "SALE_COMPLETED"
    const val SALE_VOIDED = "SALE_VOIDED"
    const val STOCK_UPDATE = "STOCK_UPDATE"
    const val PRODUCT_SYNC = "PRODUCT_SYNC"
    const val UPDATE_PRODUCT = "UPDATE_PRODUCT"
    const val UPDATE_STOCK = "UPDATE_STOCK"
}

/**
 * Network configuration constants.
 */
object SyncConfig {
    const val DEFAULT_PORT = 8080
    const val SERVICE_TYPE = "_extropos_sync._tcp."
    const val SERVICE_NAME = "ExtroPOS_Sync"
    const val HEADER_SYNC_TOKEN = "X-Sync-Token"
}

/**
 * Base class for all sync messages.
 */
data class SyncMessage<T>(
    val type: String,
    val data: T
)

/**
 * Specific data structure for stock updates.
 */
data class StockUpdateData(
    val productId: String,
    val newQuantity: BigDecimal,
    val adjustment: BigDecimal? = null,
    val isAvailable: Boolean? = null
)

