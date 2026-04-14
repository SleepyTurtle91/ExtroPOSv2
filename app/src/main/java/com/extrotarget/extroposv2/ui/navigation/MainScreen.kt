package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
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
import com.extrotarget.extroposv2.ui.sales.components.NavButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val sessionManager = viewModel.sessionManager
    val currentUser by sessionManager.currentUser.collectAsState()
    val licenseStatus by viewModel.licenseStatus.collectAsState()
    val licenseInfo by viewModel.licenseInfo.collectAsState()
    val navController = rememberNavController()
    var showActivationDialog by remember { mutableStateOf(false) }
    
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

    val screens = listOf(
        Screen.Sales,
        Screen.Tables,
        Screen.CarWash,
        Screen.Laundry,
        Screen.Inventory,
        Screen.Analytics,
        Screen.Staff,
        Screen.Settings
    )

    val activeBusinessMode by viewModel.activeBusinessMode.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F172A) // Slate 900
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
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(activeBusinessMode.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val mainScreens = listOf(
                            Screen.Sales,
                            Screen.Tables,
                            Screen.CarWash,
                            Screen.Laundry,
                            Screen.Inventory,
                            Screen.Analytics,
                            Screen.Staff
                        )

                        mainScreens.forEach { screen ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            
                            // Industry Specific Visibility Logic
                            val isVisible = when(screen) {
                                Screen.Tables -> activeBusinessMode.hasTables
                                Screen.CarWash -> activeBusinessMode == BusinessMode.CARWASH
                                Screen.Laundry -> activeBusinessMode == BusinessMode.LAUNDRY
                                Screen.Staff -> activeBusinessMode.hasStaffAssignment
                                else -> true
                            }

                            if (isVisible) {
                                NavButton(
                                    icon = if (screen == Screen.Sales && activeBusinessMode.hasTables) Icons.Default.Restaurant else screen.icon,
                                    label = if (screen == Screen.Sales && activeBusinessMode.hasTables) "FLOOR" else screen.title.uppercase(),
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
                            }
                        }
                    }

                    // Bottom Sidebar Actions (Settings & Lock)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val isSettingsSelected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true
                        
                        IconButton(
                            onClick = { 
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isSettingsSelected) Color(0xFF0F172A) else Color.White.copy(alpha = 0.05f), 
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Settings, 
                                contentDescription = "Settings", 
                                tint = if (isSettingsSelected) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                        
                        IconButton(
                            onClick = { sessionManager.logout() },
                            modifier = Modifier.size(56.dp).background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (licenseStatus is LicenseStatus.Trial) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trial Mode: ${(licenseStatus as LicenseStatus.Trial).daysRemaining} days remaining",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { showActivationDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Activate", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            NavGraph(
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

    if (showActivationDialog) {
        ActivationDialog(
            deviceId = licenseInfo?.deviceId ?: "",
            onDismiss = { showActivationDialog = false },
            onActivate = { 
                viewModel.activateLicense(it)
                showActivationDialog = false
            }
        )
    }
}

@Composable
fun ActivationDialog(
    deviceId: String,
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit
) {
    var activationKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activate License") },
        text = {
            Column {
                Text("Device ID:", style = MaterialTheme.typography.labelSmall)
                Text(deviceId, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = activationKey,
                    onValueChange = { activationKey = it },
                    label = { Text("Activation Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onActivate(activationKey) },
                enabled = activationKey.isNotBlank()
            ) {
                Text("Activate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

@Composable
fun LicenseGate(
    status: LicenseStatus,
    deviceId: String,
    onActivate: (String) -> Unit
) {
    var activationKey by remember { mutableStateOf("") }
    
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.errorContainer) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (status is LicenseStatus.Expired) "License Expired" else "Invalid License",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Please contact support to activate your POS system.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(0.6f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Device ID:", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = deviceId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = activationKey,
                        onValueChange = { activationKey = it },
                        label = { Text("Activation Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onActivate(activationKey) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = activationKey.isNotBlank()
                    ) {
                        Text("Activate Now")
                    }
                }
            }
        }
    }
}
