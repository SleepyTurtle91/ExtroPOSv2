package com.extrotarget.extroposv2.ui.settings.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.network.SyncClient
import com.extrotarget.extroposv2.core.network.SyncServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncServer: SyncServer,
    private val syncClient: SyncClient,
    private val nsdHelper: com.extrotarget.extroposv2.core.network.NsdHelper,
    private val saleRepository: com.extrotarget.extroposv2.core.data.repository.SaleRepository,
    private val branchRepository: com.extrotarget.extroposv2.core.data.repository.inventory.BranchRepository
) : ViewModel() {

    private val _isServerRunning = MutableStateFlow(syncServer.isRunning())
    val isServerRunning = _isServerRunning.asStateFlow()

    private val _discoveredMasters = MutableStateFlow<List<android.net.nsd.NsdServiceInfo>>(emptyList())
    val discoveredMasters = _discoveredMasters.asStateFlow()

    init {
        startDiscovery()
        observeRealtimeUpdates()
    }

    private fun observeRealtimeUpdates() {
        viewModelScope.launch {
            syncClient.realtimeUpdates.collect { messageJson ->
                try {
                    val message = com.google.gson.Gson().fromJson(messageJson, Map::class.java)
                    when (message["type"]) {
                        "SALE_COMPLETED" -> {
                            _syncStatus.value = "New sale synced from Master."
                        }
                        "STOCK_UPDATE" -> {
                            val data = message["data"] as Map<*, *>
                            val productId = data["productId"] as String
                            val newQuantity = java.math.BigDecimal(data["newQuantity"].toString())
                            val isAvailable = data["isAvailable"] as? Boolean
                            
                            saleRepository.updateLocalStock(productId, newQuantity, isAvailable)
                            _syncStatus.value = "Stock updated for product $productId"
                        }
                    }
                } catch (e: Exception) {
                    _syncStatus.value = "Error parsing update: ${e.message}"
                }
            }
        }
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            nsdHelper.discoverServices().collect { services ->
                _discoveredMasters.value = services
            }
        }
    }

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    private val _showConflictDialog = MutableStateFlow(false)
    val showConflictDialog = _showConflictDialog.asStateFlow()

    private var pendingMasterIp: String? = null

    private val _localIp = MutableStateFlow(getLocalIpAddress())
    val localIp = _localIp.asStateFlow()

    fun toggleServer() {
        if (syncServer.isRunning()) {
            syncServer.stop()
        } else {
            syncServer.start()
        }
        _isServerRunning.value = syncServer.isRunning()
    }

    fun syncFromMaster(masterIp: String, force: Boolean = false) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing from Master..."
            val hq = branchRepository.getHQBranch()
            val result = syncClient.syncFromMaster(masterIp, force = force, syncToken = hq?.syncToken)
            if (result.isSuccess) {
                _syncStatus.value = "Sync Successful! Connecting to Real-time..."
                viewModelScope.launch {
                    syncClient.connectToRealtime(masterIp, syncToken = hq?.syncToken)
                }
                kotlinx.coroutines.delay(2000)
                syncClient.restartApp()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                if (error.contains("DIRTY_DATA")) {
                    pendingMasterIp = masterIp
                    _showConflictDialog.value = true
                    _syncStatus.value = "Conflict: Unsynced local sales detected."
                } else {
                    _syncStatus.value = "Sync Failed: $error"
                }
            }
        }
    }

    fun confirmForceSync() {
        _showConflictDialog.value = false
        pendingMasterIp?.let { syncFromMaster(it, force = true) }
    }

    fun dismissConflict() {
        _showConflictDialog.value = false
        pendingMasterIp = null
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses.toList()
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && !addr.isLinkLocalAddress && addr.isSiteLocalAddress) {
                        return addr.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Unknown"
    }
}
