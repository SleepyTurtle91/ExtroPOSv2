package com.extrotarget.extroposv2.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Getting Started", "LHDN e-Invoicing", "Multi-Terminal Sync", "Industry Modules")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Manual & Help") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> GettingStartedHelp()
                    1 -> LhdnHelp()
                    2 -> SyncHelp()
                    3 -> IndustryHelp()
                }
            }
        }
    }
}

@Composable
fun GettingStartedHelp() {
    HelpSection("First Run", "Complete the Onboarding Wizard to set up your store details, administrator account, and license.")
    HelpSection("Sales Process", "1. Open a Shift from the Sales screen.\n2. Add items to the cart.\n3. Apply discounts if needed.\n4. Click 'Pay' and select payment method.\n5. Print receipt or generate LHDN e-invoice.")
    HelpSection("End of Day", "Perform a 'Shift Closeout' to reconcile cash and generate a Z-Report. This is essential for accounting accuracy.")
}

@Composable
fun LhdnHelp() {
    HelpSection("What is MyInvois?", "LHDN's electronic invoicing system. ExtroPOS v2 is fully integrated for real-time validation.")
    HelpSection("Setup", "Enter your TIN, BRN, and MSIC code in Settings > LHDN. Use the 'Test Connection' button to verify your API credentials.")
    HelpSection("Consolidation", "Small B2C transactions can be consolidated daily. Go to LHDN History and use the 'Consolidate' feature to group transactions under a single submission.")
}

@Composable
fun SyncHelp() {
    HelpSection("Master vs Slave", "Master terminal hosts the database. Slave terminals connect to Master over local network (WiFi).")
    HelpSection("Setup", "1. Set one terminal as 'Master' in Settings > Switch Business Mode.\n2. Set others as 'Slave'.\n3. Ensure all devices are on the same WiFi.\n4. Use 'Multi-Terminal Sync' to discover and connect.")
}

@Composable
fun IndustryHelp() {
    HelpSection("F&B (Café)", "Use 'Table Management' to track active orders. Orders are routed to Kitchen or Bar printers automatically based on product tags.")
    HelpSection("Car Wash", "Assign staff to line items to calculate commissions. Use the 'Job Queue' to monitor wash progress.")
    HelpSection("Laundry", "Weight-based items require a digital scale or manual weight entry. Customers receive WhatsApp alerts when orders are ready.")
}

@Composable
fun HelpSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
    }
}
