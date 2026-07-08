package com.extrotarget.extroposv2.ui.components.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.util.audit.SilentAuditorEngine

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.SmartToy
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun AiAssistantOverlay(
    auditorEngine: SilentAuditorEngine
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        AnimatedVisibility(
            visible = !expanded,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = { expanded = true },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .width(450.dp)
                    .heightIn(min = 300.dp, max = 600.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Silent Auditor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { expanded = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        if (isLoading) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Analyzing data...", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            val displayResponse = responseText ?: "How can I help you today? I can analyze sales, find products, or audit shifts."
                            SelectionContainer {
                                Text(
                                    text = displayResponse,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Ask about tender, stock, or Z-reports...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (query.isNotBlank()) {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            // Simple router based on query keywords for Use Case mapping
                                            responseText = when {
                                                query.contains("tender", ignoreCase = true) || query.contains("report", ignoreCase = true) -> 
                                                    auditorEngine.generateNaturalLanguageReport(query, "Tender summary data placeholder")
                                                query.contains("stock", ignoreCase = true) || query.contains("inventory", ignoreCase = true) ->
                                                    auditorEngine.getInventoryManagementAdvice("Inventory stats placeholder")
                                                query.contains("sales", ignoreCase = true) || query.contains("performance", ignoreCase = true) ->
                                                    auditorEngine.getSalesPerformanceInsight("Daily sales summary placeholder")
                                                else -> auditorEngine.generateNaturalLanguageReport(query, "General context placeholder")
                                            }
                                            query = ""
                                        } catch (e: Exception) {
                                            Timber.e(e)
                                            responseText = "I encountered an error while analyzing the data. Please check your API key."
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && query.isNotBlank(),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}
