package com.extrotarget.extroposv2.ui.components.stitch.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor

@Composable
fun StitchErrorDialog(
    title: String = "ERROR",
    message: String,
    confirmText: String = "UNDERSTOOD",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        icon = {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = StitchColor.Error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title.uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = StitchColor.OnSurfaceVariant
            )
        },
        confirmButton = {
            StitchButton(
                text = confirmText,
                onClick = onDismiss,
                containerColor = StitchColor.Error,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }
    )
}
