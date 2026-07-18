package com.extrotarget.extroposv2.ui.retail.purchase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.repository.retail.PurchaseRepository
import com.extrotarget.extroposv2.domain.retail.model.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SupplierUiState(
    val suppliers: List<Supplier> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    
    val uiState: StateFlow<SupplierUiState> = combine(
        purchaseRepository.getAllSuppliers(),
        _searchQuery
    ) { suppliers, query ->
        SupplierUiState(
            suppliers = if (query.isBlank()) suppliers else suppliers.filter { it.name.contains(query, ignoreCase = true) },
            searchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SupplierUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveSupplier(name: String, phone: String?, email: String?, address: String?) {
        viewModelScope.launch {
            val supplier = Supplier(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                email = email,
                address = address
            )
            purchaseRepository.saveSupplier(supplier)
        }
    }
}
