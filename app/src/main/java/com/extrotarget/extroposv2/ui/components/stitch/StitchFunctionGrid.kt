package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.sales.PosAction
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

data class FunctionKeyData(
    val action: PosAction,
    val label: String,
    val icon: ImageVector,
    val containerColor: Color = StitchColor.SurfaceContainerHigh,
    val contentColor: Color = StitchColor.OnSurface
)

@Composable
fun StitchFunctionGrid(
    actions: List<FunctionKeyData>,
    onActionClick: (PosAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false
    ) {
        items(actions) { data ->
            FunctionKey(data, onActionClick)
        }
    }
}

@Composable
private fun FunctionKey(
    data: FunctionKeyData,
    onClick: (PosAction) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .clickable { onClick(data.action) },
        color = data.containerColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = data.label,
                modifier = Modifier.size(20.dp),
                tint = data.contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = data.label.uppercase(),
                style = MaterialTheme.typography.labelCaps.copy(
                    fontSize = 9.sp,
                    color = data.contentColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}
