package com.extrotarget.extroposv2.ui.settings.payment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig
import com.extrotarget.extroposv2.core.data.repository.settings.DuitNowRepository
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuitNowSettingsViewModel @Inject constructor(
    private val repository: DuitNowRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val config: StateFlow<DuitNowConfig> = repository.getConfig()
        .map { it ?: DuitNowConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DuitNowConfig())

    val merchantId = flow {
        emit(securityManager.getString(SecurityManager.KEY_DUITNOW_MERCHANT_ID) ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveConfig(config: DuitNowConfig, merchantId: String) {
        viewModelScope.launch {
            securityManager.saveString(SecurityManager.KEY_DUITNOW_MERCHANT_ID, merchantId)
            repository.saveConfig(config.copy(merchantId = "******")) // Masked in DB
        }
    }
}
