package com.extrotarget.extroposv2.ui.settings.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.local.dao.AuditDao
import com.extrotarget.extroposv2.core.data.model.AuditLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AuditViewModel @Inject constructor(
    private val auditDao: AuditDao
) : ViewModel() {

    private val _selectedModule = MutableStateFlow<String?>(null)
    val selectedModule = _selectedModule.asStateFlow()

    private val _selectedStaffId = MutableStateFlow<String?>(null)
    val selectedStaffId = _selectedStaffId.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<Long, Long>>(getDefaultDateRange())
    val dateRange = _dateRange.asStateFlow()

    val auditLogs: StateFlow<List<AuditLog>> = combine(
        _selectedModule,
        _selectedStaffId,
        _dateRange
    ) { module, staffId, dates ->
        Triple(module, staffId, dates)
    }.flatMapLatest { (module, staffId, dates) ->
        auditDao.getFilteredLogs(module, staffId, dates.first, dates.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val modules: StateFlow<List<String>> = flow {
        emit(listOf("SALES", "INVENTORY", "SETTINGS", "SYSTEM", "STAFF"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("SALES", "INVENTORY", "SETTINGS", "SYSTEM"))

    val staffMembers: StateFlow<List<AuditDao.StaffSummary>> = auditDao.getStaffMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterByModule(module: String?) {
        _selectedModule.value = module
    }

    fun filterByStaff(staffId: String?) {
        _selectedStaffId.value = staffId
    }

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = Pair(start, end)
    }

    private fun getDefaultDateRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        
        return Pair(start, end)
    }
}
