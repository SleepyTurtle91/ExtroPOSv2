package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun StitchOnboardingCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tags: List<String> = emptyList(),
    selectedColor: Color = StitchColor.Primary
) {
    val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "border")
    val shadowElevation by animateFloatAsState(if (isSelected) 8f else 0f, label = "shadow")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) selectedColor else StitchColor.OutlineVariant.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(16.dp)
            ),
        color = StitchColor.Surface,
        shadowElevation = shadowElevation.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = selectedColor
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) StitchColor.PrimaryContainer else StitchColor.SurfaceContainerHigh
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) StitchColor.OnPrimaryContainer else StitchColor.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = StitchColor.OnSurfaceVariant,
                        fontSize = 14.sp
                    ),
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            color = StitchColor.SurfaceContainerHigh,
                            shape = CircleShape
                        ) {
                            Text(
                                text = tag.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelCaps.copy(
                                    fontSize = 9.sp,
                                    color = StitchColor.OnSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
