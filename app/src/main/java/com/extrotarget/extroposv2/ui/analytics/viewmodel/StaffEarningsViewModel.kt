package com.extrotarget.extroposv2.ui.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class StaffEarningsUiState(
    val staffEarnings: List<StaffEarningItem> = emptyList(),
    val isLoading: Boolean = false
)

data class StaffEarningItem(
    val staff: Staff,
    val totalEarnings: BigDecimal
)

@HiltViewModel
class StaffEarningsViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    val uiState: StateFlow<StaffEarningsUiState> = staffRepository.getAllActiveStaff()
        .flatMapLatest { staffList ->
            if (staffList.isEmpty()) return@flatMapLatest flowOf(StaffEarningsUiState())
            
            val earningFlows = staffList.map { staff ->
                staffRepository.getTotalEarningsForStaff(staff.id).map { earnings ->
                    StaffEarningItem(staff, earnings ?: BigDecimal.ZERO)
                }
            }
            
            combine(earningFlows) { earningsArray ->
                StaffEarningsUiState(staffEarnings = earningsArray.toList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StaffEarningsUiState(isLoading = true))
}
