package com.extrotarget.extroposv2.ui.settings.branch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.inventory.Branch
import com.extrotarget.extroposv2.core.data.repository.inventory.BranchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BranchSettingsUiState(
    val branches: List<Branch> = emptyList(),
    val isLoading: Boolean = false,
    val hqBranch: Branch? = null
)

@HiltViewModel
class BranchSettingsViewModel @Inject constructor(
    private val repository: BranchRepository
) : ViewModel() {

    val uiState: StateFlow<BranchSettingsUiState> = repository.getAllBranches()
        .map { branches ->
            BranchSettingsUiState(
                branches = branches,
                hqBranch = branches.find { it.isHQ }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BranchSettingsUiState())

    fun saveBranch(name: String, ipAddress: String, isHQ: Boolean, syncToken: String?) {
        viewModelScope.launch {
            val hq = uiState.value.hqBranch
            if (isHQ && hq != null && hq.id != UUID.nameUUIDFromBytes(name.toByteArray()).toString()) {
                // If making this HQ, unset current HQ
                repository.updateBranch(hq.copy(isHQ = false))
            }

            val branch = Branch(
                id = UUID.nameUUIDFromBytes(name.toByteArray()).toString(),
                name = name,
                ipAddress = ipAddress,
                isHQ = isHQ,
                syncToken = syncToken
            )
            repository.saveBranch(branch)
        }
    }

    fun deleteBranch(branch: Branch) {
        viewModelScope.launch {
            repository.deleteBranch(branch)
        }
    }
}
