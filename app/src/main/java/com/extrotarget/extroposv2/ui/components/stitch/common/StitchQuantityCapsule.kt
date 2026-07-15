package com.extrotarget.extroposv2.ui.components.stitch.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import java.math.BigDecimal

@Composable
fun StitchQuantityCapsule(
    quantity: BigDecimal,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant),
        color = StitchColor.SurfaceContainerLowest
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = StitchColor.OnSurfaceVariant
                )
            }
            
            VerticalDivider(color = StitchColor.OutlineVariant, modifier = Modifier.height(20.dp))
            
            Text(
                text = quantity.stripTrailingZeros().toPlainString(),
                modifier = Modifier.width(36.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            
            VerticalDivider(color = StitchColor.OutlineVariant, modifier = Modifier.height(20.dp))
            
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = StitchColor.OnSurfaceVariant
                )
            }
        }
    }
}
