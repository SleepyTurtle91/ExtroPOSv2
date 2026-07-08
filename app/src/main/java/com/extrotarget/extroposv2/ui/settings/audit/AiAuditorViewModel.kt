package com.extrotarget.extroposv2.ui.settings.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAuditorViewModel @Inject constructor(
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _apiKey = MutableStateFlow(securityManager.getString(SecurityManager.KEY_GEMINI_API_KEY) ?: "")
    val apiKey: StateFlow<String> = _apiKey

    private val _isAiEnabled = MutableStateFlow(_apiKey.value.isNotEmpty())
    val isAiEnabled: StateFlow<Boolean> = _isAiEnabled

    // Feature toggles
    private val _isShiftAuditEnabled = MutableStateFlow(true)
    val isShiftAuditEnabled: StateFlow<Boolean> = _isShiftAuditEnabled

    private val _isReportAssistantEnabled = MutableStateFlow(true)
    val isReportAssistantEnabled: StateFlow<Boolean> = _isReportAssistantEnabled

    private val _isInventoryAssistantEnabled = MutableStateFlow(true)
    val isInventoryAssistantEnabled: StateFlow<Boolean> = _isInventoryAssistantEnabled

    private val _isSearchOptimizationEnabled = MutableStateFlow(true)
    val isSearchOptimizationEnabled: StateFlow<Boolean> = _isSearchOptimizationEnabled

    fun updateApiKey(newKey: String) {
        viewModelScope.launch {
            securityManager.saveString(SecurityManager.KEY_GEMINI_API_KEY, newKey)
            _apiKey.value = newKey
            _isAiEnabled.value = newKey.isNotEmpty()
        }
    }

    fun toggleShiftAudit(enabled: Boolean) {
        _isShiftAuditEnabled.value = enabled
    }

    fun toggleReportAssistant(enabled: Boolean) {
        _isReportAssistantEnabled.value = enabled
    }

    fun toggleInventoryAssistant(enabled: Boolean) {
        _isInventoryAssistantEnabled.value = enabled
    }

    fun toggleSearchOptimization(enabled: Boolean) {
        _isSearchOptimizationEnabled.value = enabled
    }
}
