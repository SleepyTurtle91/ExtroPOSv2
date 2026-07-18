package com.extrotarget.extroposv2.ui.fnb.kds.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.config.AppConfig
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.local.dao.fnb.FnbStationDao
import com.extrotarget.extroposv2.domain.fnb.model.KitchenStation
import com.extrotarget.extroposv2.core.network.SyncClient
import com.extrotarget.extroposv2.core.network.SyncMessageType
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KdsUiState(
    val orders: List<KdsOrder> = emptyList(),
    val selectedStation: KitchenStation? = null,
    val stations: List<KitchenStation> = emptyList(),
    val stationItemCounts: Map<String, Int> = emptyMap()
)

data class KdsOrder(
    val sale: Sale,
    val items: List<SaleItem>
)

@HiltViewModel
class KdsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val stationDao: FnbStationDao,
    private val syncClient: SyncClient
) : ViewModel() {

    private val _selectedStationId = MutableStateFlow<String?>(null)
    private val _realtimeOrders = MutableStateFlow<List<KdsOrder>>(emptyList())

    init {
        observeRealtimeUpdates()
    }

    private fun observeRealtimeUpdates() {
        viewModelScope.launch {
            syncClient.realtimeUpdates.collect { json ->
                try {
                    val update = Gson().fromJson(json, Map::class.java)
                    if (update["type"] == SyncMessageType.SALE_COMPLETED) {
                        // In a real KDS, we might want to trigger a sound or highlight
                        // Since we are observing the DB flow in uiState,
                        // the change to DB will automatically refresh the list if the slave synced.
                        // However, for KDS standalone terminals, we might just update local DB or state.
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    val uiState: StateFlow<KdsUiState> = combine(
        saleRepository.getAllSalesWithItems(),
        stationDao.getAllStations(),
        _selectedStationId
    ) { salesWithItems, stations, selectedId ->
        val currentStation = stations.find { it.id == selectedId } ?: stations.firstOrNull()
        
        val stationItemCounts = stations.associate { station ->
            station.id to salesWithItems.sumOf { sale ->
                sale.items.count { it.printerTag.equals(station.name, ignoreCase = true) && it.status != "READY" }
            }
        }

        val filteredOrders = currentStation?.let { station ->
            salesWithItems.mapNotNull { saleWithItems ->
                val stationItems = saleWithItems.items.filter {
                    it.printerTag.equals(station.name, ignoreCase = true) && it.status != "READY"
                }
                if (stationItems.isNotEmpty() && (saleWithItems.sale.status == AppConfig.SaleStatus.COMPLETED || saleWithItems.sale.status == AppConfig.SaleStatus.PENDING)) {
                    KdsOrder(saleWithItems.sale, stationItems)
                } else null
            }.sortedBy { it.sale.timestamp }
        } ?: emptyList()

        KdsUiState(
            orders = filteredOrders,
            selectedStation = currentStation,
            stations = stations,
            stationItemCounts = stationItemCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KdsUiState())

    fun selectStation(stationId: String) {
        _selectedStationId.value = stationId
    }

    fun markOrderDone(orderId: String) {
        val currentStation = uiState.value.selectedStation ?: return
        viewModelScope.launch {
            saleRepository.updateItemsStatusByTag(orderId, currentStation.name, "READY")
        }
    }
}
