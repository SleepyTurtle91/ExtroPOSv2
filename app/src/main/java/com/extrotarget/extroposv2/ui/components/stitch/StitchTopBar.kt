package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun StitchTopBar(
    businessName: String,
    stationName: String,
    userName: String,
    userRole: String,
    isSstActive: Boolean = true,
    onSearchClick: () -> Unit = {},
    onOpenDrawerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = StitchColor.Surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Brand & Context
                Text(
                    businessName.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = StitchColor.Primary,
                        letterSpacing = 1.sp
                    )
                )
                
                Spacer(Modifier.width(16.dp))
                
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = StitchColor.OutlineVariant.copy(alpha = 0.5f)
                )
                
                Spacer(Modifier.width(16.dp))
                
                Surface(
                    color = StitchColor.SurfaceContainerLow,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        stationName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelCaps.copy(
                            fontSize = 10.sp,
                            color = StitchColor.OnSurfaceVariant
                        )
                    )
                }

                // Center: Search Spacer or Search Bar
                Spacer(Modifier.weight(1f))

                // Right: Actions & Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Action Icons
                    TopBarIcon(Icons.Default.Wifi)
                    TopBarIcon(Icons.Default.Sync)
                    TopBarIcon(Icons.Default.Notifications, hasBadge = true)

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = StitchColor.OutlineVariant.copy(alpha = 0.5f)
                    )

                    // Localized Badge
                    if (isSstActive) {
                        Surface(
                            color = StitchColor.TertiaryContainer.copy(alpha = 0.1f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.Tertiary)
                        ) {
                            Text(
                                "SST ACTIVE",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelCaps.copy(
                                    fontSize = 10.sp,
                                    color = StitchColor.TertiaryContainer
                                )
                            )
                        }
                    }

                    // Primary Action
                    Button(
                        onClick = onOpenDrawerClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StitchColor.SurfaceContainerHigh,
                            contentColor = StitchColor.Primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.Outline),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            "OPEN DRAWER",
                            style = MaterialTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Profile
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = StitchColor.PrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                userName.take(1).uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = StitchColor.OnPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
            Divider(color = StitchColor.OutlineVariant, thickness = 1.dp)
        }
    }
}

@Composable
private fun TopBarIcon(
    icon: ImageVector,
    hasBadge: Boolean = false,
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Box {
            Icon(
                icon,
                contentDescription = null,
                tint = StitchColor.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(StitchColor.Error, CircleShape)
                        .border(1.5.dp, StitchColor.Surface, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}
