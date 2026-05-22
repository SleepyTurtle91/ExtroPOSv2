package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.extrotarget.extroposv2.core.license.LicenseStatus
import com.extrotarget.extroposv2.ui.auth.LoginScreen
import com.extrotarget.extroposv2.ui.auth.MainViewModel
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.components.NavButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val sessionManager = viewModel.sessionManager
    val currentUser by sessionManager.currentUser.collectAsState()
    val licenseStatus by viewModel.licenseStatus.collectAsState()
    val licenseInfo by viewModel.licenseInfo.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val navController = rememberNavController()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.stockAlerts.collect { productName ->
            snackbarHostState.showSnackbar(
                message = "Low Stock Alert: $productName",
                duration = SnackbarDuration.Short
            )
        }
    }

    if (!isOnboardingCompleted) {
        com.extrotarget.extroposv2.ui.onboarding.OnboardingWizardScreen(
            viewModel = hiltViewModel(),
            onSetupComplete = { /* Handled by Flow */ }
        )
        return
    }

    if (currentUser == null) {
        LoginScreen(
            biometricHelper = viewModel.biometricHelper,
            onLoginSuccess = { /* SessionManager handles state */ }
        )
        return
    }

    if (licenseStatus is LicenseStatus.Expired || licenseStatus is LicenseStatus.Invalid) {
        LicenseGate(
            status = licenseStatus,
            deviceId = licenseInfo?.deviceId ?: "",
            onActivate = { viewModel.activateLicense(it) }
        )
        return
    }

    val activeBusinessMode by viewModel.activeBusinessMode.collectAsState()
    val operationMode by viewModel.operationMode.collectAsState()

    val screens = remember(activeBusinessMode, operationMode) {
        val allScreens = mutableListOf<Screen>()
        
        // 1. Sales & Operations (Hidden in Backend Mode)
        if (operationMode != com.extrotarget.extroposv2.core.data.model.settings.OperationMode.BACKEND_ONLY) {
            allScreens.add(Screen.Sales)
            if (activeBusinessMode.hasTables) allScreens.add(Screen.Tables)
            if (activeBusinessMode.hasTables) allScreens.add(Screen.Kds)
            if (activeBusinessMode == BusinessMode.CARWASH) allScreens.add(Screen.CarWash)
            if (activeBusinessMode == BusinessMode.LAUNDRY) allScreens.add(Screen.Laundry)
            if (activeBusinessMode.hasBookings) allScreens.add(Screen.HotelDashboard)
        }

        // 2. Office & Management (Always visible in Backend & Hybrid, hidden in Counter)
        if (operationMode != com.extrotarget.extroposv2.core.data.model.settings.OperationMode.POS_ONLY) {
            allScreens.add(Screen.SalesHistory)
            allScreens.add(Screen.Inventory)
            allScreens.add(Screen.Analytics)
            if (activeBusinessMode.hasStaffAssignment) allScreens.add(Screen.Staff)
        } else {
            // Limited history for counters
            allScreens.add(Screen.SalesHistory)
        }

        // 3. Settings (Always available)
        allScreens.add(Screen.Settings)
        
        allScreens.toList()
    }

    Scaffold(
        containerColor = Color(0xFF0F172A), // Slate 900
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // New Sidebar Implementation
            Surface(
                modifier = Modifier.width(100.dp).fillMaxHeight(),
                color = Color(0xFF1E293B),
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    // Logo / Business Mode Icon
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF3B82F6),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                activeBusinessMode.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    // Main Navigation
                    screens.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavButton(
                            icon = screen.icon,
                            label = screen.title,
                            isSelected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    Spacer(Modifier.weight(1f))

                    // Bottom Actions (Lock, Settings, Logout)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        IconButton(
                            onClick = { (navController.context as? androidx.activity.ComponentActivity)?.let { activity ->
                                // Trigger a lock state via ViewModel if possible, 
                                // or navigate to a lock screen. 
                                // Since we don't have a direct handle to SalesViewModel here easily, 
                                // we'll just show the concept or assume it's handled by Screen.Settings
                            } },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color(0xFF3B82F6))
                        }

                        IconButton(
                            onClick = { sessionManager.logout() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }

            // Main Content Area
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                NavGraph(
                    navController = navController,
                    sessionManager = sessionManager
                )
            }
        }
    }
}

@Composable
fun ActivationDialog(
    deviceId: String,
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Activate License", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Device ID: $deviceId", style = MaterialTheme.typography.bodySmall)
                
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("License Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = { onActivate(key) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Activate Now")
                }
            }
        }
    }
}

@Composable
fun LicenseGate(
    status: LicenseStatus,
    deviceId: String,
    onActivate: (String) -> Unit
) {
    var showActivation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF1E293B)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                text = if (status is LicenseStatus.Expired) "License Expired" else "Invalid License",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Please activate your software to continue using ExtroPOS v2.",
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 16.sp
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = { showActivation = true },
                modifier = Modifier.height(64.dp).width(300.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("ENTER LICENSE KEY", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Device ID: $deviceId",
                color = Color(0xFF475569),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showActivation) {
            ActivationDialog(
                deviceId = deviceId,
                onDismiss = { showActivation = false },
                onActivate = { 
                    onActivate(it)
                    showActivation = false
                }
            )
        }
    }
}
