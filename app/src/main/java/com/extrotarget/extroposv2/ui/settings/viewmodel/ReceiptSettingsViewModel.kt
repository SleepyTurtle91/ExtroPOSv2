package com.extrotarget.extroposv2.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptSettingsViewModel @Inject constructor(
    private val receiptDao: ReceiptDao
) : ViewModel() {

    val receiptConfig: StateFlow<ReceiptConfig> = receiptDao.getReceiptConfig()
        .map { it ?: ReceiptConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReceiptConfig())

    fun updateConfig(config: ReceiptConfig) {
        viewModelScope.launch {
            receiptDao.saveReceiptConfig(config)
        }
    }
}