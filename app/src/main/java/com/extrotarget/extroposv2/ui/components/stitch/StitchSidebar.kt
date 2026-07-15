package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Storefront
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
import com.extrotarget.extroposv2.ui.navigation.Screen
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun StitchSidebar(
    screens: List<Screen>,
    currentDestination: String?,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(80.dp)
            .fillMaxHeight(),
        color = StitchColor.InverseSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Brand Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = StitchColor.Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "V2.0 PRO",
                    style = MaterialTheme.typography.labelCaps.copy(
                        fontSize = 8.sp,
                        color = StitchColor.PrimaryFixedDim.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Navigation Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                screens.forEach { screen ->
                    val isSelected = currentDestination == screen.route
                    SidebarItem(
                        icon = screen.icon,
                        label = screen.title,
                        isSelected = isSelected,
                        onClick = { onNavigate(screen) }
                    )
                }
            }

            // Bottom Actions
            Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
            
            SidebarItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = "Logout",
                isSelected = false,
                onClick = onLogout,
                contentColor = StitchColor.Error
            )
            
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    contentColor: Color? = null
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) StitchColor.PrimaryContainer.copy(alpha = 0.15f) else Color.Transparent,
        label = "bg"
    )
    val tint by animateColorAsState(
        contentColor ?: if (isSelected) StitchColor.OnPrimaryContainer else StitchColor.SurfaceVariant.copy(alpha = 0.7f),
        label = "tint"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(StitchColor.Primary)
                    .align(Alignment.CenterStart)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelCaps.copy(
                    fontSize = 9.sp,
                    color = tint
                )
            )
        }
    }
}
