package com.extrotarget.extroposv2.ui.components.standard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.ExtroPOSV2Theme
import com.extrotarget.extroposv2.ui.components.stitch.common.StitchLoader

@Preview(showBackground = true)
@Composable
fun PreviewStandardLoadingScreen() {
    ExtroPOSV2Theme {
        StandardLoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStandardErrorScreen() {
    ExtroPOSV2Theme {
        StandardErrorScreen(message = "Printer is out of paper. Please check the hardware.", onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStandardConfirmationDialog() {
    ExtroPOSV2Theme {
        StandardConfirmationDialog(
            title = "Void Sale",
            message = "Are you sure you want to void this entire sale?",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true
        )
    }
}

@Composable
fun StandardLoadingScreen(
    message: String = "Loading..."
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        StitchLoader(message = message)
    }
}

@Composable
fun StandardErrorScreen(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ERROR OCCURRED",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        if (onRetry != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("RETRY", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StandardConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "CONFIRM",
    dismissLabel: String = "CANCEL",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title.uppercase(), fontWeight = FontWeight.Black) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = if (isDestructive) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) 
                         else ButtonDefaults.buttonColors()
            ) {
                Text(confirmLabel.uppercase(), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel.uppercase(), fontWeight = FontWeight.Bold)
            }
        }
    )
}
