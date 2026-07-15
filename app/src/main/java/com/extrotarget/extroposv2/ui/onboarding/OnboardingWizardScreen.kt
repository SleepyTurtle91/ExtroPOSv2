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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import com.extrotarget.extroposv2.core.data.repository.platform.WorkspaceRepository
import com.extrotarget.extroposv2.core.data.seeder.DataSeeder
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps
import com.extrotarget.extroposv2.ui.components.stitch.StitchOnboardingCard
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButton
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchOutlinedButton
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchTextField
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchButtonSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val isTrialStarted: Boolean = false,
    // Step 4: Generation
    val generationProgress: Float = 0f,
    val generationStatus: String = ""
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
    private val workspaceRepository: WorkspaceRepository,
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
        if (_uiState.value.currentStep < 3) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0 && _uiState.value.currentStep < 3) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    private suspend fun runWorkspaceGeneration(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(currentStep = 3) }

        val tasks = listOf(
            "Configuring ${state.businessMode.name} Personality..." to 0.2f,
            "Setting up permissions & policies..." to 0.4f,
            "Building task-oriented dashboard..." to 0.6f,
            "Optimizing for tablet operation..." to 0.8f,
            "Securing workspace DNA..." to 1.0f
        )

        for ((status, progress) in tasks) {
            _uiState.update { it.copy(generationStatus = status, generationProgress = progress) }
            delay(800)
        }

        workspaceRepository.initializeWorkspace(state.storeName, state.businessMode)
        settingsRepository.updateBusinessMode(state.businessMode)
        settingsRepository.setOnboardingCompleted(true)
        onSuccess()
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
                runWorkspaceGeneration(onSuccess)
            } else {
                _uiState.update { it.copy(isActivating = false, activationError = "ERROR_ACTIVATION_FAILED") }
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
            runWorkspaceGeneration(onSuccess)
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

    Scaffold(
        containerColor = StitchColor.Background
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Sidebar simulation
            Surface(
                modifier = Modifier.width(80.dp).fillMaxHeight(),
                color = StitchColor.InverseSurface
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 24.dp)) {
                    Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = StitchColor.Primary) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("SETUP", style = MaterialTheme.typography.labelCaps.copy(fontSize = 8.sp, color = Color.White))
                }
            }

            // Main Content Area
            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(StitchColor.SurfaceContainerLow)) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.currentStep < 3) {
                        OnboardingHeader(currentStep = uiState.currentStep)
                        Spacer(Modifier.height(48.dp))
                    }

                    AnimatedContent(
                        targetState = uiState.currentStep,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "StepTransition"
                    ) { step ->
                        when (step) {
                            0 -> StoreDetailsStep(uiState, viewModel)
                            1 -> AdminAccountStep(uiState, viewModel)
                            2 -> ActivationStep(uiState, viewModel, onSetupComplete)
                            3 -> GenerationStep(uiState)
                        }
                    }
                }

                // Footer Actions
                if (uiState.currentStep < 3) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        color = StitchColor.Surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StitchOutlinedButton(
                                text = if (uiState.currentStep == 0) "Cancel Setup" else "Back",
                                onClick = { if (uiState.currentStep > 0) viewModel.previousStep() },
                                icon = Icons.AutoMirrored.Filled.ArrowBack
                            )

                            if (uiState.currentStep < 2) {
                                StitchButton(
                                    text = "Continue",
                                    onClick = { viewModel.nextStep() },
                                    size = StitchButtonSize.LARGE,
                                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                                    enabled = when (uiState.currentStep) {
                                        0 -> uiState.isStep1Valid
                                        1 -> uiState.isStep2Valid
                                        else -> true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingHeader(currentStep: Int) {
    val steps = listOf("Industry", "Profile", "Hardware", "Review")
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Step ${currentStep + 1} of 4",
            style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.Primary, letterSpacing = 2.sp)
        )
        Text(
            text = when(currentStep) {
                0 -> "Select Your Industry"
                1 -> "Create Admin Account"
                2 -> "Activate License"
                else -> "Preparing Workspace"
            },
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black)
        )
        Text(
            text = "Configure ExtroPOS v2 with industry-specific workflows and presets.",
            style = MaterialTheme.typography.bodyLarge.copy(color = StitchColor.OnSurfaceVariant),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun StoreDetailsStep(uiState: OnboardingUIState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)) {
        Text(
            "SELECT INDUSTRY",
            style = MaterialTheme.typography.labelCaps.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StitchOnboardingCard(
                title = "Food & Beverage",
                description = "Optimized for Restaurants, Cafes, and Quick Service.",
                icon = Icons.Default.Restaurant,
                isSelected = uiState.businessMode == BusinessMode.FNB,
                onClick = { viewModel.updateBusinessMode(BusinessMode.FNB) },
                tags = listOf("SST Ready", "KDS"),
                modifier = Modifier.weight(1f)
            )
            StitchOnboardingCard(
                title = "Retail & Grocery",
                description = "Built for high-volume scanning and inventory.",
                icon = Icons.Default.ShoppingCart,
                isSelected = uiState.businessMode == BusinessMode.RETAIL,
                onClick = { viewModel.updateBusinessMode(BusinessMode.RETAIL) },
                tags = listOf("BNM Rounding"),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StitchTextField(
                value = uiState.storeName,
                onValueChange = viewModel::updateStoreName,
                label = "Store Name",
                placeholder = "e.g. My Cafe",
                modifier = Modifier.weight(1f)
            )
            StitchTextField(
                value = uiState.regNo,
                onValueChange = viewModel::updateRegNo,
                label = "Registration No.",
                placeholder = "SSM / SST ID",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        StitchTextField(
            value = uiState.address,
            onValueChange = viewModel::updateAddress,
            label = "Full Address",
            placeholder = "Business location",
            singleLine = false,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AdminAccountStep(uiState: OnboardingUIState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 100.dp)) {
        StitchTextField(
            value = uiState.adminName,
            onValueChange = viewModel::updateAdminName,
            label = "Admin Full Name",
            leadingIcon = Icons.Default.Person,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        StitchTextField(
            value = uiState.adminUsername,
            onValueChange = viewModel::updateAdminUsername,
            label = "Username / Phone",
            leadingIcon = Icons.Default.Badge,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        StitchTextField(
            value = uiState.adminPin,
            onValueChange = viewModel::updateAdminPin,
            label = "Master PIN (4 Digits)",
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActivationStep(uiState: OnboardingUIState, viewModel: OnboardingViewModel, onSetupComplete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        StitchTextField(
            value = uiState.activationKey,
            onValueChange = viewModel::updateActivationKey,
            label = "License Key",
            placeholder = "XXXX-XXXX-XXXX",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(32.dp))

        StitchButton(
            text = "VERIFY & ACTIVATE",
            onClick = { viewModel.activateLicense(onSetupComplete) },
            size = StitchButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth(),
            containerColor = StitchColor.Primary,
            enabled = uiState.activationKey.isNotBlank() && !uiState.isActivating
        )

        Spacer(Modifier.height(16.dp))

        StitchOutlinedButton(
            text = "START 14-DAY TRIAL",
            onClick = { viewModel.startTrial(onSetupComplete) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isActivating
        )
    }
}

@Composable
fun GenerationStep(uiState: OnboardingUIState) {
    val checklistItems = listOf(
        "Configuring personality",
        "Setting up permissions",
        "Building dashboard",
        "Optimizing for tablet",
        "Securing workspace DNA"
    )
    
    val currentProgress = uiState.generationProgress

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier.size(160.dp),
                color = StitchColor.Primary,
                strokeWidth = 12.dp,
                trackColor = StitchColor.SurfaceContainerLow
            )
            Text(
                text = "${(currentProgress * 100).toInt()}%",
                color = StitchColor.OnSurface,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = "PREPARING YOUR WORKSPACE",
            style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant, letterSpacing = 2.sp)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Column(
            modifier = Modifier.width(300.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            checklistItems.forEachIndexed { index, item ->
                val itemProgress = (index + 1).toFloat() / checklistItems.size
                val isDone = currentProgress >= itemProgress
                val isCurrent = currentProgress >= (index.toFloat() / checklistItems.size) && !isDone

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isDone) Icons.Default.CheckCircle else Icons.Default.Circle,
                        contentDescription = null,
                        tint = if (isDone) StitchColor.Tertiary else if (isCurrent) StitchColor.Primary else StitchColor.OutlineVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.uppercase(),
                        color = if (isDone || isCurrent) StitchColor.OnSurface else StitchColor.Outline,
                        style = MaterialTheme.typography.labelCaps.copy(
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
