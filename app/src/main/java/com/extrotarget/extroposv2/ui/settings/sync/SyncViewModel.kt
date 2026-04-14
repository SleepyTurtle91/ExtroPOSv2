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
    private val saleRepository: com.extrotarget.extroposv2.core.data.repository.SaleRepository
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
            syncClient.realtimeUpdates.collect { message ->
                // Handle realtime message (e.g., "SALE_COMPLETED")
                _syncStatus.value = "Real-time update received: $message"
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

    fun syncFromMaster(masterIp: String) {
        viewModelScope.launch {
            // ... (existing conflict check)
            
            _syncStatus.value = "Syncing from Master..."
            val result = syncClient.syncFromMaster(masterIp)
            if (result.isSuccess) {
                _syncStatus.value = "Sync Successful! Connecting to Real-time..."
                // Start WebSocket connection after successful DB sync
                viewModelScope.launch {
                    syncClient.connectToRealtime(masterIp)
                }
                kotlinx.coroutines.delay(2000)
                syncClient.restartApp()
            } else {
                _syncStatus.value = "Sync Failed: ${result.exceptionOrNull()?.message}"
            }
        }
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
