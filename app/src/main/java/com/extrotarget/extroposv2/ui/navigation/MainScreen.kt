package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.extrotarget.extroposv2.core.license.LicenseStatus
import com.extrotarget.extroposv2.ui.auth.LoginScreen
import com.extrotarget.extroposv2.ui.auth.MainViewModel

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
        LoginScreen(onLoginSuccess = { /* SessionManager handles state */ })
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("ExtroPOS - ${currentUser?.name ?: ""}") 
                },
                actions = {
                    IconButton(onClick = { sessionManager.logout() }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock Screen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                screens.filter { it !is Screen.InventoryAnalytics && it !is Screen.StaffEarnings && it !is Screen.Backup && it !is Screen.ReceiptSettings }.forEach { screen ->
                    // RBAC check for specific screens
                    val canAccess = when (screen) {
                        is Screen.Settings, is Screen.Analytics, is Screen.Staff, is Screen.Inventory -> sessionManager.hasRole("ADMIN")
                        else -> true
                    }

                    if (canAccess) {
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
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
