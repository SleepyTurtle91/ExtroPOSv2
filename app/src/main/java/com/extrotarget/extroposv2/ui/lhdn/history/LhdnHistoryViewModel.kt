package com.extrotarget.extroposv2.ui.lhdn.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.local.dao.lhdn.LhdnDao
import com.extrotarget.extroposv2.core.data.model.lhdn.EInvoiceStatus
import com.extrotarget.extroposv2.core.data.model.lhdn.SaleEInvoiceSubmission
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LhdnHistoryViewModel @Inject constructor(
    private val lhdnDao: LhdnDao,
    private val lhdnRepository: LhdnRepository
) : ViewModel() {

    val submissions: StateFlow<List<SaleEInvoiceSubmission>> = lhdnDao.getAllSubmissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun forceRepoll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val pending = lhdnDao.getSubmissionsByStatus(EInvoiceStatus.SUBMITTED)
            pending.forEach { submission ->
                submission.uuid?.let { uuid ->
                    lhdnRepository.pollDocumentStatus(uuid)
                }
            }
            _isRefreshing.value = false
        }
    }
}
