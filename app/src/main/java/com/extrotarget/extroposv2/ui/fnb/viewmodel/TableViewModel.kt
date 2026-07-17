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
    private val tableRepository: TableRepository,
    private val printReceiptUseCase: com.extrotarget.extroposv2.core.domain.usecase.PrintReceiptUseCase
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

    fun addTable(name: String, code: String, capacity: Int = 4, zone: String = "Indoor") {
        viewModelScope.launch {
            if (tableRepository.existsByName(name, zone)) {
                // Handle duplicate name (e.g., via a UI state error)
                return@launch
            }
            
            val newTable = Table(
                id = UUID.randomUUID().toString(),
                code = code,
                name = name,
                capacity = capacity,
                status = TableStatus.AVAILABLE,
                zone = zone,
                displayOrder = tables.value.size
            )
            tableRepository.addTable(newTable)
        }
    }

    fun bulkAddTables(
        prefix: String,
        startNumber: Int,
        count: Int,
        capacity: Int = 4,
        zone: String = "Indoor"
    ) {
        viewModelScope.launch {
            var addedCount = 0
            val existingNames = mutableListOf<String>()
            
            for (i in 0 until count) {
                val number = startNumber + i
                val tableName = if (prefix.endsWith(" ")) "$prefix$number" else "$prefix $number"
                val tableCode = "${prefix.take(1).uppercase()}$number"
                
                if (tableRepository.existsByName(tableName, zone)) {
                    existingNames.add(tableName)
                    continue
                }
                
                val newTable = Table(
                    id = UUID.randomUUID().toString(),
                    code = tableCode,
                    name = tableName,
                    capacity = capacity,
                    status = TableStatus.AVAILABLE,
                    zone = zone,
                    displayOrder = tables.value.size + addedCount
                )
                tableRepository.addTable(newTable)
                addedCount++
            }
            // TODO: Expose bulk add summary to UI (addedCount, existingNames)
        }
    }

    fun duplicateTable(table: Table) {
        viewModelScope.launch {
            var newName = "${table.name} (Copy)"
            var newCode = "${table.code}C"
            var counter = 1
            
            while (tableRepository.existsByName(newName, table.zone)) {
                newName = "${table.name} (Copy $counter)"
                counter++
            }
            
            val duplicatedTable = table.copy(
                id = UUID.randomUUID().toString(),
                name = newName,
                code = newCode,
                status = TableStatus.AVAILABLE,
                currentSaleId = null,
                currentBillAmount = null,
                hasUnsentItems = false,
                displayOrder = tables.value.size
            )
            tableRepository.addTable(duplicatedTable)
        }
    }

    fun updateTableOrder(tableId: String, newOrder: Int) {
        viewModelScope.launch {
            tableRepository.getTable(tableId)?.let { table ->
                tableRepository.updateTable(table.copy(displayOrder = newOrder))
            }
        }
    }

    fun deleteTable(tableId: String) {
        viewModelScope.launch {
            val table = tableRepository.getTable(tableId)
            if (table != null && table.status == TableStatus.AVAILABLE) {
                tableRepository.deleteTable(tableId)
            } else {
                // TODO: Show error "Cannot delete occupied/reserved table"
            }
        }
    }

    fun printTableQr(table: Table, qrContent: String) {
        viewModelScope.launch {
            printReceiptUseCase.printTableQr(table.name, qrContent)
        }
    }
}
