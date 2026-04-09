package com.extrotarget.extroposv2.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.settings.TaxConfig
import com.extrotarget.extroposv2.core.data.repository.settings.TaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxSettingsViewModel @Inject constructor(
    private val taxRepository: TaxRepository
) : ViewModel() {

    val taxConfig: StateFlow<TaxConfig> = taxRepository.getTaxConfig()
        .map { it ?: TaxConfig() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaxConfig()
        )

    fun updateTaxConfig(config: TaxConfig) {
        viewModelScope.launch {
            taxRepository.updateTaxConfig(config)
        }
    }
}
