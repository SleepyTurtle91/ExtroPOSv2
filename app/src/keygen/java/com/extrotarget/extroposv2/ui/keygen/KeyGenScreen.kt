package com.extrotarget.extroposv2.ui.keygen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.config.AppConfig
import com.extrotarget.extroposv2.ui.components.qr.QrCodeView
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGenScreen() {
    var deviceId by remember { mutableStateOf("") }
    var generatedKey by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ExtroPOS v2 - Activation Key Generator", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Device Identification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = deviceId,
                        onValueChange = { deviceId = it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. 5F3A8B2C...") },
                        label = { Text("Device ID") },
                        singleLine = true,
                        trailingIcon = {
                            if (deviceId.isNotEmpty()) {
                                IconButton(onClick = { 
                                    deviceId = ""
                                    generatedKey = ""
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (deviceId.isNotBlank()) {
                                generatedKey = generateKey(deviceId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("GENERATE ACTIVATION KEY", fontWeight = FontWeight.Black)
                    }
                }
            }
            
            if (generatedKey.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "ACTIVATION KEY",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        SelectionContainer {
                            Text(
                                generatedKey,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF047857),
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        QrCodeView(
                            content = generatedKey,
                            size = 400,
                            modifier = Modifier.size(200.dp)
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { clipboardManager.setText(AnnotatedString(generatedKey)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("COPY")
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            Text(
                "INTERNAL TOOLS ONLY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

private fun generateKey(id: String): String {
    val salt = AppConfig.Security.CRYPTO_SALT
    val bytes = (id + salt).toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }.take(16).uppercase()
}
