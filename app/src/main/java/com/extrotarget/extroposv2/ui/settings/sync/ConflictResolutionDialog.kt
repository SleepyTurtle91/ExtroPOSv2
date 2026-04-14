package com.extrotarget.extroposv2.ui.settings.sync

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ConflictResolutionDialog(
    onDismiss: () -> Unit,
    onOverwrite: () -> Unit,
    onKeepLocal: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Conflict Detected") },
        text = {
            Text("This terminal has unsynced local transactions. Syncing from the Master terminal will OVERWRITE these transactions. What would you like to do?")
        },
        confirmButton = {
            Button(
                onClick = onOverwrite,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Overwrite Local Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepLocal) {
                Text("Keep Local (Cancel Sync)")
            }
        }
    )
}
