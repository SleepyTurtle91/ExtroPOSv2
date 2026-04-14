package com.extrotarget.extroposv2.ui.loyalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyConfig
import com.extrotarget.extroposv2.core.data.repository.loyalty.LoyaltyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoyaltySettingsViewModel @Inject constructor(
    private val repository: LoyaltyRepository
) : ViewModel() {

    val config: StateFlow<LoyaltyConfig?> = repository.getConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveConfig(config: LoyaltyConfig) {
        viewModelScope.launch {
            repository.saveConfig(config)
        }
    }
}
