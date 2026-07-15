package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun StitchKpiCard(
    label: String,
    value: String,
    trend: String? = null,
    isTrendUp: Boolean = true,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    contentColor: Color = StitchColor.Primary
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        color = StitchColor.SurfaceContainerLowest,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant)
                )
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = StitchColor.Outline, modifier = Modifier.size(20.dp))
                }
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = if (label.contains("Sales", ignoreCase = true)) StitchColor.Primary else StitchColor.OnSurface,
                        fontSize = 28.sp
                    )
                )
                
                if (trend != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (isTrendUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isTrendUp) StitchColor.Tertiary else StitchColor.Error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isTrendUp) StitchColor.Tertiary else StitchColor.Error
                            )
                        )
                    }
                }
            }
        }
    }
}
