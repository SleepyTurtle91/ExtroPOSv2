package com.extrotarget.extroposv2.ui.loyalty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyConfig
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltySettingsScreen(
    viewModel: LoyaltySettingsViewModel = hiltViewModel()
) {
    val configState by viewModel.config.collectAsState()
    val config = configState ?: LoyaltyConfig()

    var isEnabled by remember(config) { mutableStateOf(config.isEnabled) }
    var pointsPerUnit by remember(config) { mutableStateOf(config.pointsPerCurrencyUnit.toString()) }
    var valuePerPoint by remember(config) { mutableStateOf(config.redemptionValuePerPoint.toString()) }
    var minRedeem by remember(config) { mutableStateOf(config.minPointsToRedeem.toString()) }
    var silverThreshold by remember(config) { mutableStateOf(config.silverThreshold.toString()) }
    var goldThreshold by remember(config) { mutableStateOf(config.goldThreshold.toString()) }
    var silverMultiplier by remember(config) { mutableStateOf(config.silverMultiplier.toString()) }
    var goldMultiplier by remember(config) { mutableStateOf(config.goldMultiplier.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Loyalty Program Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Loyalty Program", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
            }

            if (isEnabled) {
                OutlinedTextField(
                    value = pointsPerUnit,
                    onValueChange = { pointsPerUnit = it },
                    label = { Text("Points Earned per Currency Unit (e.g., 1 for RM 1)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valuePerPoint,
                    onValueChange = { valuePerPoint = it },
                    label = { Text("Redemption Value per Point (e.g., 0.01 for 100pts = RM1)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minRedeem,
                    onValueChange = { minRedeem = it },
                    label = { Text("Minimum Points to Redeem") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                Text("Tiered Rewards", style = MaterialTheme.typography.titleMedium)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = silverThreshold,
                        onValueChange = { silverThreshold = it },
                        label = { Text("Silver Threshold (Pts)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = silverMultiplier,
                        onValueChange = { silverMultiplier = it },
                        label = { Text("Silver Multiplier (e.g., 1.2)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goldThreshold,
                        onValueChange = { goldThreshold = it },
                        label = { Text("Gold Threshold (Pts)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = goldMultiplier,
                        onValueChange = { goldMultiplier = it },
                        label = { Text("Gold Multiplier (e.g., 1.5)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.saveConfig(
                        LoyaltyConfig(
                            isEnabled = isEnabled,
                            pointsPerCurrencyUnit = pointsPerUnit.toBigDecimalOrNull() ?: BigDecimal.ONE,
                            redemptionValuePerPoint = valuePerPoint.toBigDecimalOrNull() ?: BigDecimal("0.01"),
                            minPointsToRedeem = minRedeem.toBigDecimalOrNull() ?: BigDecimal("100"),
                            silverThreshold = silverThreshold.toBigDecimalOrNull() ?: BigDecimal("1000"),
                            goldThreshold = goldThreshold.toBigDecimalOrNull() ?: BigDecimal("5000"),
                            silverMultiplier = silverMultiplier.toBigDecimalOrNull() ?: BigDecimal("1.2"),
                            goldMultiplier = goldMultiplier.toBigDecimalOrNull() ?: BigDecimal("1.5")
                        )
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Settings")
            }
        }
    }
}
