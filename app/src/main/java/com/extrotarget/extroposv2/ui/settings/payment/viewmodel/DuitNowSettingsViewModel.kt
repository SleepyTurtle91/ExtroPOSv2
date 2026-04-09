package com.extrotarget.extroposv2.ui.settings.payment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig
import com.extrotarget.extroposv2.core.data.repository.settings.DuitNowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuitNowSettingsViewModel @Inject constructor(
    private val repository: DuitNowRepository
) : ViewModel() {

    val config: StateFlow<DuitNowConfig> = repository.getConfig()
        .map { it ?: DuitNowConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DuitNowConfig())

    fun saveConfig(config: DuitNowConfig) {
        viewModelScope.launch {
            repository.saveConfig(config)
        }
    }
}
