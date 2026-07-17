package com.extrotarget.extroposv2.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun ConfirmRestoreDemoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Restore Demo Database?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This will PERMANENTLY remove all current business data, including products, categories, customers, inventory, and sales history.",
                    fontWeight = FontWeight.Bold
                )
                Text("Your application settings, users (staff), license, and printer configurations will be preserved.")
                Text("Demo sample data will then be recreated. This action cannot be undone.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Restore Demo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RestoreProgressDialog() {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text("Restoring Demo Database...", fontWeight = FontWeight.Bold)
                Text("Please wait, do not close the app.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { }
    )
}

@Composable
fun RestoreSuccessDialog(
    summary: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981)) },
        title = { Text("Restore Successful") },
        text = {
            Text(summary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
