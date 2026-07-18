package com.extrotarget.extroposv2.ui.settings.diagnostics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.platform.DeviceIdentity
import com.extrotarget.extroposv2.core.platform.DeviceIdentityManager
import com.extrotarget.extroposv2.core.platform.DiagnosticsManager
import com.extrotarget.extroposv2.core.platform.DiagnosticsReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticsUiState(
    val identity: DeviceIdentity? = null,
    val report: DiagnosticsReport? = null,
    val isRunning: Boolean = false
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val deviceIdentityManager: DeviceIdentityManager,
    private val diagnosticsManager: DiagnosticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true)
            val identity = deviceIdentityManager.getDeviceIdentity()
            val report = diagnosticsManager.runFullDiagnostics()
            _uiState.value = DiagnosticsUiState(
                identity = identity,
                report = report,
                isRunning = false
            )
        }
    }
}
