package com.extrotarget.extroposv2.ui.fnb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus
import com.extrotarget.extroposv2.core.data.repository.fnb.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TableViewModel @Inject constructor(
    private val tableRepository: TableRepository
) : ViewModel() {

    private val _selectedZone = MutableStateFlow("Indoor")
    val selectedZone: StateFlow<String> = _selectedZone.asStateFlow()

    val tables: StateFlow<List<Table>> = combine(
        tableRepository.allTables,
        _selectedZone
    ) { allTables, zone ->
        allTables.filter { it.zone == zone }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val zones: StateFlow<List<String>> = tableRepository.allTables
        .map { allTables -> 
            val uniqueZones = allTables.map { it.zone }.distinct()
            if (uniqueZones.isEmpty()) listOf("Indoor", "Outdoor", "VIP") else uniqueZones
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Indoor", "Outdoor", "VIP"))

    fun selectZone(zone: String) {
        _selectedZone.value = zone
    }

    fun addTable(name: String, capacity: Int = 4, zone: String = "Indoor") {
        viewModelScope.launch {
            val newTable = Table(
                id = UUID.randomUUID().toString(),
                name = name,
                capacity = capacity,
                status = TableStatus.AVAILABLE,
                zone = zone
            )
            tableRepository.addTable(newTable)
        }
    }

    fun updateTableStatus(table: Table, status: TableStatus) {
        viewModelScope.launch {
            tableRepository.updateTable(table.copy(status = status))
        }
    }
}