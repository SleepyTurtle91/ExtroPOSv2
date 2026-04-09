package com.extrotarget.extroposv2.core.hardware.scale

import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface ScaleInterface {
    /**
     * Connects to the scale hardware.
     */
    suspend fun connect(): Boolean

    /**
     * Disconnects from the scale hardware.
     */
    suspend fun disconnect()

    /**
     * Returns a flow of weight updates.
     */
    fun getWeightFlow(): Flow<BigDecimal>

    /**
     * Triggers a single weight reading.
     */
    suspend fun readWeight(): BigDecimal

    /**
     * Tares the scale to zero.
     */
    suspend fun tare()
}
