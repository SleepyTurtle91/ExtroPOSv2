package com.extrotarget.extroposv2.ui.components.stitch.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

enum class StitchButtonSize(val height: Int, val radius: Int) {
    LARGE(64, 16),
    STANDARD(48, 8)
}

@Composable
fun StitchButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: StitchButtonSize = StitchButtonSize.STANDARD,
    containerColor: Color = StitchColor.Primary,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(size.height.dp),
        shape = RoundedCornerShape(size.radius.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            style = if (size == StitchButtonSize.LARGE) 
                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            else
                MaterialTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun StitchOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: StitchButtonSize = StitchButtonSize.STANDARD,
    contentColor: Color = StitchColor.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(size.height.dp),
        shape = RoundedCornerShape(size.radius.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor
        ),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold)
        )
    }
}
