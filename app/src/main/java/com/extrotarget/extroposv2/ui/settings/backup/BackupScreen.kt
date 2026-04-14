package com.extrotarget.extroposv2.ui.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Lock
import android.text.format.Formatter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.ui.settings.viewmodel.BackupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        viewModel.backup(outputStream)
                    }
                }
            }
        }
    )

    val masterExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        viewModel.exportMasterData(outputStream)
                    }
                }
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        viewModel.restore(inputStream)
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Backup,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Protect your POS data. Create database backups or export all transaction data to encrypted ZIP files.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(24.dp))

            // Encryption Password Field
            OutlinedTextField(
                value = uiState.exportPassword,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Export Password (Optional for Encryption)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                singleLine = true,
                supportingText = { Text("If set, master export ZIP will be encrypted with AES-256.") }
            )

            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        val fileName = "extropos_backup_${System.currentTimeMillis()}.db"
                        backupLauncher.launch(fileName) 
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("DB Backup", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = {
                        val fileName = "extropos_master_data_${System.currentTimeMillis()}.zip"
                        masterExportLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Export ZIP", style = MaterialTheme.typography.labelLarge)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { restoreLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Restore from File")
            }

            if (uiState.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            uiState.message?.let { message ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = viewModel::clearMessage) {
                            Text("OK")
                        }
                    }
                }
            }

            if (uiState.recentBackups.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("Recent Local Backups", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.recentBackups) { backup ->
                        ListItem(
                            headlineContent = { Text(backup.name, style = MaterialTheme.typography.bodyMedium) },
                            supportingContent = { 
                                Text(
                                    "${Formatter.formatFileSize(context, backup.size)} • ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(backup.date)}",
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { coroutineScope.launch { viewModel.restoreFromFile(backup) } }) {
                                        Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteBackup(backup) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
