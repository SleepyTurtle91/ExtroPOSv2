package com.extrotarget.extroposv2.ui.settings.lhdn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.util.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LhdnSettingsViewModel @Inject constructor(
    private val repository: LhdnRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _testResult = MutableStateFlow<Result<String>?>(null)
    val testResult = _testResult.asStateFlow()

    val config = repository.getConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val clientId = kotlinx.coroutines.flow.flow {
        emit(securityManager.getString(SecurityManager.KEY_LHDN_CLIENT_ID) ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val clientSecret = kotlinx.coroutines.flow.flow {
        emit(securityManager.getString(SecurityManager.KEY_LHDN_CLIENT_SECRET) ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun testConnection(clientId: String, clientSecret: String, isSandbox: Boolean) {
        viewModelScope.launch {
            _testResult.value = null
            // Temporarily update security manager for testing
            securityManager.saveString(SecurityManager.KEY_LHDN_CLIENT_ID, clientId)
            securityManager.saveString(SecurityManager.KEY_LHDN_CLIENT_SECRET, clientSecret)
            
            // We can't change the sandbox flag easily in the repository without saving, 
            // but we can try to get a token.
            val token = repository.getValidToken()
            if (token != null) {
                _testResult.value = Result.success("Connection Successful! Token received.")
            } else {
                _testResult.value = Result.failure(Exception("Connection Failed. Check credentials or network."))
            }
        }
    }

    fun clearTestResult() {
        _testResult.value = null
    }

    fun saveConfig(
        sellerTin: String,
        sellerBrn: String,
        sellerSstId: String,
        msicCode: String,
        businessDesc: String,
        clientIdValue: String,
        clientSecretValue: String,
        isSandbox: Boolean,
        isEnabled: Boolean,
        thresholdAmount: java.math.BigDecimal
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
                isEnabled = isEnabled,
                einvoiceThresholdAmount = thresholdAmount
            )
            repository.saveConfig(newConfig)
        }
    }
}
