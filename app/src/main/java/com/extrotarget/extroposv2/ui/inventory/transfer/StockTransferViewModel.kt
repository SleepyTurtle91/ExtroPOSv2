package com.extrotarget.extroposv2.ui.inventory.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.inventory.Branch
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransfer
import com.extrotarget.extroposv2.core.data.model.inventory.StockTransferStatus
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.inventory.BranchRepository
import com.extrotarget.extroposv2.core.data.repository.inventory.StockTransferRepository
import com.extrotarget.extroposv2.core.network.BranchSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

data class StockTransferUiState(
    val branches: List<Branch> = emptyList(),
    val products: List<Product> = emptyList(),
    val transfers: List<StockTransfer> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StockTransferViewModel @Inject constructor(
    private val branchRepository: BranchRepository,
    private val productRepository: ProductRepository,
    private val transferRepository: StockTransferRepository,
    private val branchSyncManager: BranchSyncManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StockTransferUiState> = combine(
        branchRepository.getAllBranches(),
        productRepository.getAllProducts(),
        transferRepository.getAllTransfers(),
        _isLoading,
        _error
    ) { branches, products, transfers, loading, error ->
        StockTransferUiState(branches, products, transfers, loading, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StockTransferUiState())

    fun createTransfer(fromBranchId: String, toBranchId: String, productId: String, productName: String, quantity: BigDecimal) {
        viewModelScope.launch {
            _isLoading.value = true
            val transfer = StockTransfer(
                id = UUID.randomUUID().toString(),
                fromBranchId = fromBranchId,
                toBranchId = toBranchId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                status = StockTransferStatus.PENDING
            )
            
            val result = branchSyncManager.initiateStockTransfer(transfer)
            if (result.isSuccess) {
                _error.value = null
            } else {
                _error.value = "Sync Failed: ${result.exceptionOrNull()?.message}. Transfer saved locally."
                transferRepository.createTransfer(transfer)
            }
            _isLoading.value = false
        }
    }

    fun syncStockWithHQ() {
        viewModelScope.launch {
            _isLoading.value = true
            branchSyncManager.syncStockWithHQ()
            _isLoading.value = false
        }
    }
}
