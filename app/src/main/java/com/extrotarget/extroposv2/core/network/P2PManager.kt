package com.extrotarget.extroposv2.core.network

import com.extrotarget.extroposv2.core.data.model.settings.TerminalRole
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class P2PManager @Inject constructor(
    private val syncServer: SyncServer,
    private val syncClient: SyncClient,
    private val nsdHelper: NsdHelper,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initialize() {
        scope.launch {
            settingsRepository.terminalRole.collectLatest { role ->
                manageP2PServices(role)
            }
        }
    }

    private fun manageP2PServices(role: TerminalRole) {
        Timber.d("P2PManager: Managing services for role: $role")
        
        if (role == TerminalRole.MASTER) {
            if (!syncServer.isRunning()) {
                syncServer.start()
                Timber.d("P2PManager: Master Server started")
            }
        } else {
            if (syncServer.isRunning()) {
                syncServer.stop()
            }
            
            startDiscoveryAndConnect()
        }
    }

    private fun startDiscoveryAndConnect() {
        scope.launch {
            nsdHelper.discoverServices().collectLatest { services ->
                val master = services.firstOrNull()
                if (master != null) {
                    val host = master.host?.hostAddress
                    if (host != null) {
                        Timber.d("P2PManager: Found Master at $host. Connecting...")
                        // In a real scenario, we might want to check syncToken or deviceId
                        syncClient.connectToRealtime(host)
                    }
                }
            }
        }
    }

    fun stopAll() {
        syncServer.stop()
        // Client stops when scope is cancelled or manually if needed
    }
}
