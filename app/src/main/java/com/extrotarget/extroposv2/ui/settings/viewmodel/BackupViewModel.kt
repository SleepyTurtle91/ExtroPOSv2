package com.extrotarget.extroposv2.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.util.backup.BackupManager
import com.extrotarget.extroposv2.core.util.exporter.MasterExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.Application
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val isRestoreSuccessful: Boolean = false,
    val recentBackups: List<BackupFile> = emptyList(),
    val exportPassword: String = ""
)

data class BackupFile(
    val name: String,
    val path: String,
    val size: Long,
    val date: Date
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val application: Application,
    private val backupManager: BackupManager,
    private val masterExportManager: MasterExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRecentBackups()
    }

    fun loadRecentBackups() {
        val backupDir = File(application.getExternalFilesDir(null), "backups")
        if (backupDir.exists()) {
            val files = backupDir.listFiles()?.filter { it.extension == "zip" || it.extension == "db" }
                ?.map { 
                    BackupFile(
                        name = it.name,
                        path = it.absolutePath,
                        size = it.length(),
                        date = Date(it.lastModified())
                    )
                }?.sortedByDescending { it.date } ?: emptyList()
            
            _uiState.update { it.copy(recentBackups = files) }
        }
    }

    fun deleteBackup(backup: BackupFile) {
        val file = File(backup.path)
        if (file.exists() && file.delete()) {
            loadRecentBackups()
        }
    }

    suspend fun restoreFromFile(backup: BackupFile) {
        _uiState.update { it.copy(isLoading = true, message = "Restoring from local backup...", isRestoreSuccessful = false) }
        val file = File(backup.path)
        val result = if (file.exists()) {
            backupManager.restoreDatabase(file.inputStream())
        } else {
            Result.failure(Exception("File not found"))
        }
        
        _uiState.update {
            it.copy(
                isLoading = false,
                message = if (result.isSuccess) "Restore successful. Application must restart." else "Restore failed: ${result.exceptionOrNull()?.message}",
                isError = result.isFailure,
                isRestoreSuccessful = result.isSuccess
            )
        }
    }

    suspend fun backup(outputStream: OutputStream) {
        _uiState.update { it.copy(isLoading = true, message = "Creating backup...", isRestoreSuccessful = false) }
        val result = backupManager.backupDatabase(outputStream)
        _uiState.update {
            it.copy(
                isLoading = false,
                message = if (result.isSuccess) "Backup created successfully" else "Backup failed: ${result.exceptionOrNull()?.message}",
                isError = result.isFailure
            )
        }
    }

    suspend fun restore(inputStream: InputStream) {
        _uiState.update { it.copy(isLoading = true, message = "Restoring backup...", isRestoreSuccessful = false) }
        val result = backupManager.restoreDatabase(inputStream)
        _uiState.update {
            it.copy(
                isLoading = false,
                message = if (result.isSuccess) "Restore successful. Application must restart." else "Restore failed: ${result.exceptionOrNull()?.message}",
                isError = result.isFailure,
                isRestoreSuccessful = result.isSuccess
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(exportPassword = password) }
    }

    suspend fun exportMasterData(outputStream: OutputStream) {
        val password = _uiState.value.exportPassword
        _uiState.update { it.copy(isLoading = true, message = "Exporting data...", isRestoreSuccessful = false) }
        
        val result = if (password.isNotEmpty()) {
            masterExportManager.exportEncryptedData(outputStream, password)
        } else {
            masterExportManager.exportAllData(outputStream)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                message = if (result.isSuccess) {
                    if (password.isNotEmpty()) "Encrypted master export completed successfully" 
                    else "Master export completed successfully"
                } else "Export failed: ${result.exceptionOrNull()?.message}",
                isError = result.isFailure
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
