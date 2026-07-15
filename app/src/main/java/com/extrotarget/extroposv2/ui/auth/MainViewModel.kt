package com.extrotarget.extroposv2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.auth.BiometricHelper
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.license.LicenseInfo
import com.extrotarget.extroposv2.core.license.LicenseManager
import com.extrotarget.extroposv2.core.license.LicenseStatus
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.core.platform.WorkspaceEngine
import com.extrotarget.extroposv2.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val licenseManager: LicenseManager,
    private val settingsRepository: SettingsRepository,
    private val saleRepository: SaleRepository,
    val biometricHelper: BiometricHelper,
    val workspaceEngine: WorkspaceEngine
) : ViewModel() {
    
    // ... rest of existing properties ...

    val allowedScreens: StateFlow<List<Screen>> = combine(
        sessionManager.currentUser,
        workspaceEngine.workspaceConfig,
        settingsRepository.operationMode
    ) { user, config, opMode ->
        val screens = mutableListOf<Screen>()
        if (user == null) return@combine emptyList<Screen>()
        
        // Always include Dashboard
        screens.add(Screen.Dashboard)
        
        // Sales / Operations
        if (opMode != com.extrotarget.extroposv2.core.data.model.settings.OperationMode.BACKEND_ONLY) {
            if (workspaceEngine.hasPermission(com.extrotarget.extroposv2.core.security.Permission.VIEW_SALES)) {
                screens.add(Screen.Sales)
            }
            // Mode specific screens
            val mode = config?.businessMode ?: BusinessMode.RETAIL
            if (mode.hasTables) screens.add(Screen.Tables)
            if (mode.hasTables) screens.add(Screen.Kds)
            if (mode == BusinessMode.CARWASH) screens.add(Screen.CarWash)
            if (mode == BusinessMode.LAUNDRY) screens.add(Screen.Laundry)
            if (mode.hasBookings) screens.add(Screen.HotelDashboard)
        }

        // Management / Backoffice
        if (opMode != com.extrotarget.extroposv2.core.data.model.settings.OperationMode.POS_ONLY) {
            if (workspaceEngine.hasPermission(com.extrotarget.extroposv2.core.security.Permission.VIEW_INVENTORY)) {
                screens.add(Screen.Inventory)
            }
            if (workspaceEngine.hasPermission(com.extrotarget.extroposv2.core.security.Permission.VIEW_ANALYTICS)) {
                screens.add(Screen.Analytics)
            }
            if (workspaceEngine.hasPermission(com.extrotarget.extroposv2.core.security.Permission.VIEW_STAFF)) {
                screens.add(Screen.Staff)
            }
        }
        
        screens.add(Screen.SalesHistory)
        screens.add(Screen.Settings)
        
        screens.distinctBy { it.route }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Screen.Dashboard, Screen.Settings))
    
    val licenseInfo: StateFlow<LicenseInfo?> = licenseManager.licenseInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val licenseStatus: StateFlow<LicenseStatus> = licenseManager.licenseInfo
        .map { licenseManager.getLicenseStatus(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LicenseStatus.Invalid)

    val activeBusinessMode: StateFlow<BusinessMode> = settingsRepository.activeBusinessMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessMode.RETAIL)

    val operationMode: StateFlow<com.extrotarget.extroposv2.core.data.model.settings.OperationMode> = settingsRepository.operationMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.extrotarget.extroposv2.core.data.model.settings.OperationMode.HYBRID)

    val terminalRole: StateFlow<com.extrotarget.extroposv2.core.data.model.settings.TerminalRole> = settingsRepository.terminalRole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.extrotarget.extroposv2.core.data.model.settings.TerminalRole.MASTER)

    val isOnboardingCompleted: StateFlow<Boolean> = settingsRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true) // Default to true to avoid flicker if not needed

    val stockAlerts = saleRepository.stockAlerts

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
