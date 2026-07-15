package com.extrotarget.extroposv2.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.extrotarget.extroposv2.ui.auth.LoginScreen
import com.extrotarget.extroposv2.ui.auth.MainViewModel
import com.extrotarget.extroposv2.ui.components.stitch.StitchSidebar
import com.extrotarget.extroposv2.ui.components.stitch.StitchTopBar
import com.extrotarget.extroposv2.ui.onboarding.OnboardingWizardScreen
import com.extrotarget.extroposv2.ui.theme.StitchColor

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val currentUser by viewModel.sessionManager.currentUser.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    if (!isOnboardingCompleted) {
        OnboardingWizardScreen(
            viewModel = hiltViewModel(),
            onSetupComplete = { /* Navigation handled by State */ }
        )
        return
    }

    if (currentUser == null) {
        LoginScreen(
            biometricHelper = viewModel.biometricHelper,
            onLoginSuccess = { /* State handles */ }
        )
        return
    }

    val screens by viewModel.allowedScreens.collectAsState()

    Scaffold(
        containerColor = StitchColor.Background
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Shared Stitch Sidebar
            StitchSidebar(
                screens = screens,
                currentDestination = currentDestination,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = { viewModel.logout() }
            )

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // Shared Stitch TopBar
                StitchTopBar(
                    businessName = "ExtroPOS V2",
                    stationName = "Station 01",
                    userName = currentUser?.name ?: "User",
                    userRole = currentUser?.role ?: "Staff"
                )

                // Content Graph
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        navController = navController,
                        sessionManager = viewModel.sessionManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
