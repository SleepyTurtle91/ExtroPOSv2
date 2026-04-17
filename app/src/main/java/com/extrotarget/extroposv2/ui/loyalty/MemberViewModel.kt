package com.extrotarget.extroposv2.ui.loyalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import com.extrotarget.extroposv2.core.data.model.loyalty.MemberWithHistory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.extrotarget.extroposv2.core.data.repository.loyalty.LoyaltyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LoyaltyUiState(
    val members: List<Member> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedMember: Member? = null,
    val memberHistory: MemberWithHistory? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MemberViewModel @Inject constructor(
    private val repository: LoyaltyRepository,
    private val branchSyncManager: com.extrotarget.extroposv2.core.network.BranchSyncManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMemberId = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(false)

    val uiState: StateFlow<LoyaltyUiState> = combine(
        repository.getAllMembers(),
        _searchQuery,
        _selectedMemberId,
        _isSyncing
    ) { members, query, selectedId, syncing ->
        LoyaltyUiState(
            members = if (query.isBlank()) members else members.filter { 
                it.name.contains(query, ignoreCase = true) || it.phoneNumber.contains(query)
            },
            searchQuery = query,
            selectedMember = members.find { it.id == selectedId },
            isLoading = syncing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoyaltyUiState())

    val memberHistory: StateFlow<MemberWithHistory?> = _selectedMemberId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else {
            val transactionsFlow = repository.getTransactionsForMember(id)
            val salesFlow = repository.getSalesForMember(id)
            
            combine(transactionsFlow, salesFlow) { transactions, sales ->
                val member = repository.getMemberById(id)
                member?.let { MemberWithHistory(it, transactions, sales) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun registerMember(name: String, phoneNumber: String, email: String?) {
        viewModelScope.launch {
            val newMember = Member(
                id = UUID.randomUUID().toString(),
                name = name,
                phoneNumber = phoneNumber,
                email = email
            )
            repository.saveMember(newMember)
        }
    }

    fun selectMember(memberId: String?) {
        _selectedMemberId.value = memberId
        if (memberId != null) {
            viewModelScope.launch {
                _isSyncing.value = true
                branchSyncManager.pullMemberFromHQ(memberId)
                _isSyncing.value = false
            }
        }
    }
}
