package com.extrotarget.extroposv2.core.hardware.scale

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MockScale @Inject constructor() : ScaleInterface {
    private val _weight = MutableStateFlow(BigDecimal.ZERO)
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun connect(): Boolean {
        if (isRunning) return true
        isRunning = true
        simulateWeightChanges()
        return true
    }

    override suspend fun disconnect() {
        isRunning = false
    }

    override fun getWeightFlow(): Flow<BigDecimal> = _weight.asStateFlow()

    override suspend fun readWeight(): BigDecimal {
        delay(500) // Simulate hardware latency
        return _weight.value
    }

    override suspend fun tare() {
        _weight.value = BigDecimal.ZERO
    }

    private fun simulateWeightChanges() {
        scope.launch {
            while (isRunning) {
                delay(1500)
                // Randomly change weight between 0.00 and 10.00 kg
                val newWeight = BigDecimal(Random.nextDouble(0.0, 10.0))
                    .setScale(2, RoundingMode.HALF_EVEN)
                _weight.value = newWeight
            }
        }
    }
}
