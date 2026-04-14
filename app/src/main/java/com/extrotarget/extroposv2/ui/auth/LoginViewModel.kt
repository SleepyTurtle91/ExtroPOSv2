package com.extrotarget.extroposv2.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onPinChange(newPin: String) {
        if (newPin.length <= 6) {
            _uiState.update { it.copy(pin = newPin, error = null) }
            if (newPin.length >= 4) {
                verifyPin(newPin)
            }
        }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val staff = staffRepository.getStaffByPin(pin)
            if (staff != null) {
                sessionManager.login(staff)
                _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else if (pin.length == 6) {
                _uiState.update { it.copy(isLoading = false, error = "Invalid PIN", pin = "") }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}