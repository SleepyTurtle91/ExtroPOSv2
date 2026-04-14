package com.extrotarget.extroposv2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.license.LicenseInfo
import com.extrotarget.extroposv2.core.license.LicenseManager
import com.extrotarget.extroposv2.core.license.LicenseStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val licenseManager: LicenseManager
) : ViewModel() {
    
    val licenseInfo: StateFlow<LicenseInfo?> = licenseManager.licenseInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val licenseStatus: StateFlow<LicenseStatus> = licenseManager.licenseInfo
        .map { licenseManager.getLicenseStatus(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LicenseStatus.Invalid)

    init {
        viewModelScope.launch {
            licenseManager.initializeTrial()
        }
    }

    fun activateLicense(key: String) {
        viewModelScope.launch {
            licenseManager.activate(key)
        }
    }

    fun logout() {
        sessionManager.logout()
    }
}
