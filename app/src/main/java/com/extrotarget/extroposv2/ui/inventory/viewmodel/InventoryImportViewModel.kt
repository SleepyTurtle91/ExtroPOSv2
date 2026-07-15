package com.extrotarget.extroposv2.ui.inventory.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.util.importer.ProductImportManager
import com.extrotarget.extroposv2.core.util.audit.SilentAuditorEngine
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
    private val importManager: ProductImportManager,
    private val auditorEngine: SilentAuditorEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState = _uiState.asStateFlow()
    
    private var lastUri: Uri? = null

    fun importCsv(uri: Uri) {
        lastUri = uri
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

    fun repairWithAi() {
        val uri = lastUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Repairing CSV data...") }
            try {
                val corruptedCsv = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Failed to open CSV file")

                val schema = "Name, SKU, Barcode, Price, TaxRate, Stock, MinStock, CategoryID, Description, PrinterTag, CommRate, FixedComm, ImageUrl, Available, WeightBased"
                val repairedCsv = auditorEngine.repairCsvTemplate(corruptedCsv, schema)

                _uiState.update { it.copy(message = "Importing repaired data...") }
                val byteStream = java.io.ByteArrayInputStream(repairedCsv.toByteArray(Charsets.UTF_8))
                val result = importManager.importFromCsv(byteStream)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = if (result.isSuccess) "Successfully repaired and imported ${result.getOrNull()} products" else "Import failed: ${result.exceptionOrNull()?.message}",
                        isError = result.isFailure
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "AI Repair failed: ${e.message}",
                        isError = true
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}