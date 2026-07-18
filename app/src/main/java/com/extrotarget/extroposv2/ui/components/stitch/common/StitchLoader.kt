package com.extrotarget.extroposv2.ui.components.stitch.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor

@Composable
fun StitchLoader(
    modifier: Modifier = Modifier,
    message: String? = null,
    color: Color = StitchColor.Primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        if (message != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = message.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = StitchColor.OnSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun StitchFullscreenLoader(
    message: String? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        StitchLoader(message = message)
    }
}
