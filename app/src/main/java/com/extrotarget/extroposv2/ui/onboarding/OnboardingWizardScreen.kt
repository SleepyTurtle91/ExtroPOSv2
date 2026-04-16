package com.extrotarget.extroposv2.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import com.extrotarget.extroposv2.core.data.seeder.DataSeeder
import com.extrotarget.extroposv2.ui.sales.BusinessMode

// --- UI State ---

data class OnboardingUIState(
    val currentStep: Int = 0,
    // Step 1: Store Details
    val storeName: String = "",
    val regNo: String = "",
    val address: String = "",
    val contactNo: String = "",
    val businessMode: BusinessMode = BusinessMode.RETAIL,
    // Step 2: Admin Details
    val adminName: String = "",
    val adminUsername: String = "",
    val adminPin: String = "",
    // Step 3: Activation
    val activationKey: String = "",
    val isActivating: Boolean = false,
    val activationError: String? = null,
    val isTrialStarted: Boolean = false
) {
    val isStep1Valid: Boolean
        get() = storeName.isNotBlank() && regNo.isNotBlank() && contactNo.isNotBlank()

    val isStep2Valid: Boolean
        get() = adminName.isNotBlank() && adminUsername.isNotBlank() && adminPin.length == 4

    val isStep3Valid: Boolean
        get() = activationKey.isNotBlank() || isTrialStarted
}

// --- ViewModel ---

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dataSeeder: DataSeeder
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUIState())
    val uiState: StateFlow<OnboardingUIState> = _uiState.asStateFlow()

    fun updateStoreName(value: String) = _uiState.update { it.copy(storeName = value) }
    fun updateRegNo(value: String) = _uiState.update { it.copy(regNo = value) }
    fun updateAddress(value: String) = _uiState.update { it.copy(address = value) }
    fun updateContactNo(value: String) = _uiState.update { it.copy(contactNo = value) }
    fun updateBusinessMode(value: BusinessMode) = _uiState.update { it.copy(businessMode = value) }

    fun updateAdminName(value: String) = _uiState.update { it.copy(adminName = value) }
    fun updateAdminUsername(value: String) = _uiState.update { it.copy(adminUsername = value) }
    fun updateAdminPin(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(adminPin = value) }
        }
    }

    fun updateActivationKey(value: String) = _uiState.update { it.copy(activationKey = value) }

    fun nextStep() {
        if (_uiState.value.currentStep < 2) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun activateLicense(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActivating = true, activationError = null) }
            delay(1000)
            
            if (_uiState.value.activationKey == "EXTRO-PRO-2024") {
                val state = _uiState.value
                dataSeeder.seedForMode(
                    mode = state.businessMode,
                    adminName = state.adminName,
                    adminUsername = state.adminUsername,
                    adminPin = state.adminPin
                )
                settingsRepository.updateBusinessMode(state.businessMode)
                settingsRepository.setOnboardingCompleted(true)
                onSuccess()
            } else {
                _uiState.update { it.copy(isActivating = false, activationError = "Invalid Activation Key. Please contact support.") }
            }
        }
    }

    fun startTrial(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActivating = true) }
            val state = _uiState.value
            dataSeeder.seedForMode(
                mode = state.businessMode,
                adminName = state.adminName,
                adminUsername = state.adminUsername,
                adminPin = state.adminPin
            )
            settingsRepository.updateBusinessMode(state.businessMode)
            settingsRepository.setOnboardingCompleted(true)
            delay(500)
            onSuccess()
        }
    }
}

// --- UI Components ---

@Composable
fun OnboardingWizardScreen(
    viewModel: OnboardingViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Slate 900
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .wrapContentHeight()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate 800
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(40.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Progress
                OnboardingHeader(currentStep = uiState.currentStep)
                
                Spacer(modifier = Modifier.height(48.dp))

                // Step Content
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "StepTransition"
                ) { step ->
                    when (step) {
                        0 -> StoreDetailsStep(uiState, viewModel)
                        1 -> AdminAccountStep(uiState, viewModel)
                        2 -> ActivationStep(uiState, viewModel, onSetupComplete)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.currentStep > 0) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.height(56.dp).weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Back", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    if (uiState.currentStep < 2) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.height(56.dp).weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                            enabled = when (uiState.currentStep) {
                                0 -> uiState.isStep1Valid
                                1 -> uiState.isStep2Valid
                                else -> true
                            }
                        ) {
                            Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingHeader(currentStep: Int) {
    val steps = listOf("Store", "Admin", "License")
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome to ExtroPOS v2",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Complete the initial setup to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            steps.forEachIndexed { index, title ->
                StepIndicator(
                    index = index + 1,
                    title = title,
                    isSelected = currentStep >= index,
                    isLast = index == steps.size - 1
                )
            }
        }
    }
}

@Composable
fun StepIndicator(index: Int, title: String, isSelected: Boolean, isLast: Boolean) {
    val activeColor = Color(0xFF0EA5E9)
    val inactiveColor = Color.White.copy(alpha = 0.1f)
    val color = if (isSelected) activeColor else inactiveColor

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected && index <= 0 /* This would be for completed state */) {
                   Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = index.toString(),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
            )
        }
        
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(2.dp)
                    .padding(horizontal = 8.dp)
                    .background(if (isSelected) activeColor.copy(alpha = 0.5f) else inactiveColor)
            )
        }
    }
}

@Composable
fun StoreDetailsStep(uiState: OnboardingUIState, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Select Primary Business Type",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BusinessMode.values().forEach { mode ->
                val isSelected = uiState.businessMode == mode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) mode.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .border(
                            1.dp,
                            if (isSelected) mode.color else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.updateBusinessMode(mode) },
                    color = Color.Transparent
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            mode.icon, 
                            contentDescription = null, 
                            tint = if (isSelected) mode.color else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            mode.displayName.split(" ").first(),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        WizardTextField(
            label = "Store Name",
            value = uiState.storeName,
            onValueChange = viewModel::updateStoreName,
            icon = Icons.Default.Store
        )
        WizardTextField(
            label = "Registration / SST No.",
            value = uiState.regNo,
            onValueChange = viewModel::updateRegNo,
            icon = Icons.Default.Business
        )
        WizardTextField(
            label = "Contact Number",
            value = uiState.contactNo,
            onValueChange = viewModel::updateContactNo,
            icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )
        WizardTextField(
            label = "Full Address",
            value = uiState.address,
            onValueChange = viewModel::updateAddress,
            icon = Icons.Default.LocationOn,
            singleLine = false,
            minLines = 3
        )
    }
}

@Composable
fun AdminAccountStep(uiState: OnboardingUIState, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Create a SuperAdmin account to manage the system and authorize restricted actions.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        WizardTextField(
            label = "Admin Full Name",
            value = uiState.adminName,
            onValueChange = viewModel::updateAdminName,
            icon = Icons.Default.Person
        )
        WizardTextField(
            label = "Username",
            value = uiState.adminUsername,
            onValueChange = viewModel::updateAdminUsername,
            icon = Icons.Default.Badge
        )
        WizardTextField(
            label = "Security PIN (4 digits)",
            value = uiState.adminPin,
            onValueChange = viewModel::updateAdminPin,
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.NumberPassword,
            visualTransformation = PasswordVisualTransformation()
        )
    }
}

@Composable
fun ActivationStep(
    uiState: OnboardingUIState,
    viewModel: OnboardingViewModel,
    onSetupComplete: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Final Step: Software Activation",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            WizardTextField(
                label = "Activation Key",
                value = uiState.activationKey,
                onValueChange = viewModel::updateActivationKey,
                icon = Icons.Default.VpnKey,
                placeholder = "XXXX-XXXX-XXXX-XXXX"
            )
            if (uiState.activationError != null) {
                Text(
                    text = uiState.activationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }
        }

        Button(
            onClick = { viewModel.activateLicense(onSetupComplete) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
            enabled = uiState.activationKey.isNotBlank() && !uiState.isActivating
        ) {
            if (uiState.isActivating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Verify & Activate System", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
            Text("OR", modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.labelMedium)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
        }

        OutlinedButton(
            onClick = { viewModel.startTrial(onSetupComplete) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
            enabled = !uiState.isActivating
        ) {
            Text("Start 30-Day Free Trial", fontSize = 18.sp)
        }
    }
}

@Composable
fun WizardTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF0EA5E9),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedLabelColor = Color(0xFF38BDF8),
            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
            cursorColor = Color(0xFF0EA5E9)
        )
    )
}

@Preview(device = Devices.TABLET, showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun OnboardingPreview() {
    // Note: This preview will not work correctly in a real build environment
    // as it lacks a provided SettingsRepository, but it helps with UI design.
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Onboarding Wizard Preview (Tablet)", color = Color.White)
        }
    }
}
