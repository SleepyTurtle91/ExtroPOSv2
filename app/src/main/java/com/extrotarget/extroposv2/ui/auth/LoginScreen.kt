package com.extrotarget.extroposv2.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.auth.BiometricHelper

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    biometricHelper: BiometricHelper, // Assuming it's provided or can be injected
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // Auto-show biometric prompt if available
    LaunchedEffect(uiState.isBiometricAvailable) {
        if (uiState.isBiometricAvailable && context is FragmentActivity) {
            biometricHelper.showBiometricPrompt(
                activity = context,
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = { _, _ -> /* Handle error */ },
                onFailed = { /* Handle failure */ }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Enter POS PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // PIN Display (dots)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            repeat(6) { index ->
                val isFilled = index < uiState.pin.length
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Numeric Keypad
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("FINGERPRINT", "0", "DEL")
            )
            
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { key ->
                        if (key == "FINGERPRINT") {
                            if (uiState.isBiometricAvailable) {
                                PinKeyButton(
                                    text = key,
                                    onClick = {
                                        if (context is FragmentActivity) {
                                            biometricHelper.showBiometricPrompt(
                                                activity = context,
                                                onSuccess = { viewModel.onBiometricSuccess() },
                                                onError = { _, _ -> },
                                                onFailed = { }
                                            )
                                        }
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(80.dp))
                            }
                        } else if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(80.dp))
                        } else {
                            PinKeyButton(
                                text = key,
                                onClick = {
                                    if (key == "DEL") {
                                        if (uiState.pin.isNotEmpty()) {
                                            viewModel.onPinChange(uiState.pin.dropLast(1))
                                        }
                                    } else {
                                        viewModel.onPinChange(uiState.pin + key)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeyButton(
    text: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape
    ) {
        when (text) {
            "DEL" -> Icon(Icons.Default.Backspace, contentDescription = "Delete")
            "FINGERPRINT" -> Icon(Icons.Default.Fingerprint, contentDescription = "Biometric")
            else -> {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}