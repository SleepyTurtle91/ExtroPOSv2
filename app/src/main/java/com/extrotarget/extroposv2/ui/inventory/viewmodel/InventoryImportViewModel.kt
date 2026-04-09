package com.extrotarget.extroposv2.ui.inventory.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.util.importer.ProductImportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class InventoryImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importManager: ProductImportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState = _uiState.asStateFlow()

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Importing products...") }
            
            val result = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                importManager.importFromCsv(inputStream)
            } ?: Result.failure(Exception("Failed to open file"))

            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = if (result.isSuccess) "Successfully imported ${result.getOrNull()} products" else "Import failed: ${result.exceptionOrNull()?.message}",
                    isError = result.isFailure
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}