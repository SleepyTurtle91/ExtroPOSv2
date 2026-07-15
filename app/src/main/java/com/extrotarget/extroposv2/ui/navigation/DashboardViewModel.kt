package com.extrotarget.extroposv2.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.platform.WorkspaceEngine
import com.extrotarget.extroposv2.core.platform.models.DashboardActionId
import com.extrotarget.extroposv2.core.platform.models.DashboardConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workspaceEngine: WorkspaceEngine,
    val sessionManager: SessionManager
) : ViewModel() {

    val dashboardConfig: StateFlow<DashboardConfig> = workspaceEngine.workspaceConfig
        .map { workspaceEngine.getDashboardConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), workspaceEngine.getDashboardConfig())

    val userName: StateFlow<String> = sessionManager.currentUser
        .map { it?.name ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    val businessName: StateFlow<String> = workspaceEngine.workspaceConfig
        .map { it?.businessName ?: "ExtroPOS Workspace" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ExtroPOS Workspace")
}
