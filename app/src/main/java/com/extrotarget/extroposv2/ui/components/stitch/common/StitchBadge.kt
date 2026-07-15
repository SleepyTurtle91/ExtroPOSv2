package com.extrotarget.extroposv2.ui.components.stitch.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun StitchBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = StitchColor.Tertiary,
    contentColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelCaps.copy(
                fontSize = 9.sp,
                color = contentColor
            )
        )
    }
}

@Composable
fun StitchOutlinedBadge(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = StitchColor.Primary,
    contentColor: Color = StitchColor.Primary
) {
    Box(
        modifier = modifier
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelCaps.copy(
                fontSize = 9.sp,
                color = contentColor
            )
        )
    }
}
