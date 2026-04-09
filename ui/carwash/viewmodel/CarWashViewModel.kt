package com.extrotarget.extroposv2.ui.carwash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.repository.carwash.CarWashRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.ui.carwash.staff.StaffViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CarWashViewModel @Inject constructor(
    private val repository: CarWashRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    val allJobs: StateFlow<List<CarWashJob>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addJob(plate: String, model: String?, service: String, price: BigDecimal) {
        viewModelScope.launch {
            val job = CarWashJob(
                id = UUID.randomUUID().toString(),
                plateNumber = plate,
                carModel = model,
                serviceName = service,
                price = price
            )
            repository.createJob(job)
        }
    }

    fun startJob(jobId: String, staffId: String, staffName: String) {
        viewModelScope.launch {
            repository.startJob(jobId, staffId, staffName)
        }
    }

    fun completeJob(jobId: String) {
        viewModelScope.launch {
            val job = allJobs.value.find { it.id == jobId }
            if (job?.assignedStaffId != null) {
                // Default 20% commission for washers in Malaysian SME context
                val commissionAmount = job.price.multiply(BigDecimal("0.20"))
                val record = CommissionRecord(
                    id = UUID.randomUUID().toString(),
                    staffId = job.assignedStaffId,
                    saleId = job.id,
                    amount = commissionAmount,
                    timestamp = System.currentTimeMillis(),
                    description = "Wash commission for ${job.plateNumber}"
                )
                staffRepository.addCommissionRecords(listOf(record))
            }
            repository.completeJob(jobId)
        }
    }

    fun cancelJob(jobId: String) {
        viewModelScope.launch {
            repository.cancelJob(jobId)
        }
    }
}