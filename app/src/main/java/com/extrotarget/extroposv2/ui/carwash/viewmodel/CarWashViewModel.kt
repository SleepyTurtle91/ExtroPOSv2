package com.extrotarget.extroposv2.ui.carwash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import com.extrotarget.extroposv2.core.data.repository.carwash.CarWashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CarWashUiState(
    val queuedJobs: List<CarWashJob> = emptyList(),
    val inProgressJobs: List<CarWashJob> = emptyList(),
    val completedJobs: List<CarWashJob> = emptyList()
)

@HiltViewModel
class CarWashViewModel @Inject constructor(
    private val repository: CarWashRepository
) : ViewModel() {

    val uiState: StateFlow<CarWashUiState> = repository.allJobs
        .map { jobs ->
            CarWashUiState(
                queuedJobs = jobs.filter { it.status == CarWashStatus.QUEUED },
                inProgressJobs = jobs.filter { it.status == CarWashStatus.IN_PROGRESS },
                completedJobs = jobs.filter { it.status == CarWashStatus.COMPLETED }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarWashUiState())

    fun updateJobStatus(jobId: String, status: CarWashStatus) {
        viewModelScope.launch {
            when (status) {
                CarWashStatus.COMPLETED -> repository.completeJob(jobId)
                CarWashStatus.CANCELLED -> repository.cancelJob(jobId)
                else -> { /* No-op for other manual transitions */ }
            }
        }
    }

    fun assignStaff(jobId: String, staffId: String, staffName: String) {
        viewModelScope.launch {
            repository.startJob(jobId, staffId, staffName)
        }
    }
}
