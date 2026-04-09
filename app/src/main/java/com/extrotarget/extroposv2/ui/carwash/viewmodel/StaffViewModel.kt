package com.extrotarget.extroposv2.ui.carwash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

data class StaffWithEarnings(
    val staff: Staff,
    val totalEarnings: BigDecimal = BigDecimal.ZERO
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _staffWithEarningsList = MutableStateFlow<List<StaffWithEarnings>>(emptyList())
    val staffWithEarningsList: StateFlow<List<StaffWithEarnings>> = _staffWithEarningsList.asStateFlow()

    val staffList: StateFlow<List<Staff>> = staffRepository.getAllActiveStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStaffData()
    }

    private fun loadStaffData() {
        viewModelScope.launch {
            staffRepository.getAllActiveStaff().flatMapLatest { staffList ->
                if (staffList.isEmpty()) return@flatMapLatest flowOf(emptyList<StaffWithEarnings>())
                
                val flows = staffList.map { staff ->
                    staffRepository.getTotalEarningsForStaff(staff.id).map { earnings ->
                        StaffWithEarnings(staff, earnings ?: BigDecimal.ZERO)
                    }
                }
                combine(flows) { it.toList() }
            }.collect {
                _staffWithEarningsList.value = it
            }
        }
    }

    fun addStaff(name: String, role: String, phone: String?) {
        viewModelScope.launch {
            val newStaff = Staff(
                id = UUID.randomUUID().toString(),
                name = name,
                role = role,
                phone = phone
            )
            staffRepository.saveStaff(newStaff)
        }
    }

    fun updateStaff(staff: Staff) {
        viewModelScope.launch {
            staffRepository.saveStaff(staff)
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            staffRepository.deleteStaff(staff)
        }
    }
}