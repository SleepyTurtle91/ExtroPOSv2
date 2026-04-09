package com.extrotarget.extroposv2.ui.settings.lhdn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LhdnSettingsViewModel @Inject constructor(
    private val repository: LhdnRepository
) : ViewModel() {

    val config = repository.getConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveConfig(
        sellerTin: String,
        sellerBrn: String,
        sellerSstId: String,
        msicCode: String,
        businessDesc: String,
        clientId: String,
        clientSecret: String,
        isSandbox: Boolean
    ) {
        viewModelScope.launch {
            val current = config.value
            val newConfig = LhdnConfig(
                id = 1,
                sellerTin = sellerTin,
                sellerBrn = sellerBrn,
                sellerSstId = sellerSstId,
                msicCode = msicCode,
                businessActivityDesc = businessDesc,
                clientId = clientId,
                clientSecret = clientSecret,
                isSandbox = isSandbox
            )
            repository.saveConfig(newConfig)
        }
    }
}
