package com.extrotarget.extroposv2.ui.settings.autocount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.settings.AutoCountConfig
import com.extrotarget.extroposv2.core.data.repository.settings.AutoCountRepository
import com.extrotarget.extroposv2.core.network.api.autocount.AutoCountApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AutoCountUiState(
    val config: AutoCountConfig = AutoCountConfig(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AutoCountSettingsViewModel @Inject constructor(
    private val repository: AutoCountRepository,
    private val api: AutoCountApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoCountUiState())
    val uiState: StateFlow<AutoCountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getConfig().collect { config ->
                if (config != null) {
                    _uiState.update { it.copy(config = config) }
                }
            }
        }
    }

    fun onConfigChange(config: AutoCountConfig) {
        _uiState.update { it.copy(config = config) }
    }

    fun saveConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                repository.saveConfig(_uiState.value.config)
                _uiState.update { it.copy(isLoading = false, successMessage = "Configuration saved successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to save: ${e.localizedMessage}") }
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val config = _uiState.value.config
                val response = api.login(
                    username = config.username,
                    password = config.password
                )
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.access_token
                    val expiry = System.currentTimeMillis() + (response.body()!!.expires_in * 1000L)
                    
                    val updatedConfig = config.copy(
                        syncToken = token,
                        tokenExpiry = expiry
                    )
                    repository.saveConfig(updatedConfig)
                    
                    _uiState.update { it.copy(
                        isLoading = false, 
                        successMessage = "Connection successful! Token retrieved.",
                        config = updatedConfig
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Connection failed: ${response.code()} ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.localizedMessage}") }
            }
        }
    }
}
