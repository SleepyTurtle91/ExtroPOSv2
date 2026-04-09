package com.extrotarget.extroposv2.ui.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.util.backup.DatabaseBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: DatabaseBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState = _uiState.asStateFlow()

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Exporting...") }
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val result = backupManager.exportDatabase(outputStream)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = if (result.isSuccess) "Backup exported successfully" else "Export failed: ${result.exceptionOrNull()?.message}",
                        isError = result.isFailure
                    )
                }
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Importing...") }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val result = backupManager.importDatabase(inputStream)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = if (result.isSuccess) "Backup restored. Please restart the app." else "Restore failed: ${result.exceptionOrNull()?.message}",
                        isError = result.isFailure
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}