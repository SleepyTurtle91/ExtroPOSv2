package com.extrotarget.extroposv2.ui.settings.lhdn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LhdnSettingsViewModel @Inject constructor(
    private val repository: LhdnRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    val config = repository.getConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val clientId = kotlinx.coroutines.flow.flow {
        emit(securityManager.getString(SecurityManager.KEY_LHDN_CLIENT_ID) ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val clientSecret = kotlinx.coroutines.flow.flow {
        emit(securityManager.getString(SecurityManager.KEY_LHDN_CLIENT_SECRET) ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun saveConfig(
        sellerTin: String,
        sellerBrn: String,
        sellerSstId: String,
        msicCode: String,
        businessDesc: String,
        clientIdValue: String,
        clientSecretValue: String,
        isSandbox: Boolean,
        isEnabled: Boolean
    ) {
        viewModelScope.launch {
            // Save sensitive credentials to encrypted storage
            securityManager.saveString(SecurityManager.KEY_LHDN_CLIENT_ID, clientIdValue)
            securityManager.saveString(SecurityManager.KEY_LHDN_CLIENT_SECRET, clientSecretValue)

            val newConfig = LhdnConfig(
                id = 1,
                sellerTin = sellerTin,
                sellerBrn = sellerBrn,
                sellerSstId = sellerSstId,
                msicCode = msicCode,
                businessActivityDesc = businessDesc,
                clientId = null, // Don't store in DB
                clientSecret = null, // Don't store in DB
                isSandbox = isSandbox,
                isEnabled = isEnabled
            )
            repository.saveConfig(newConfig)
        }
    }
}
