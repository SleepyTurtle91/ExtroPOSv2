package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.sales.BusinessMode

@Composable
fun SidebarNavigation(
    activeMode: BusinessMode,
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onToggleSettings: (Boolean) -> Unit,
    onLock: () -> Unit
) {
    Surface(
        modifier = Modifier.width(100.dp).fillMaxHeight(),
        color = Color(0xFF1E293B),
        tonalElevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(activeMode.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NavButton(
                    icon = if (activeMode.hasTables) Icons.Default.Restaurant else Icons.Default.ShoppingCart,
                    label = if (activeMode.hasTables) "FLOOR" else "SALES",
                    isSelected = activeTab == "pos",
                    onClick = { onTabSelected("pos") }
                )
                
                if (activeMode.hasTables) {
                    NavButton(
                        icon = Icons.Default.Layers,
                        label = "TABLES",
                        isSelected = activeTab == "tables",
                        onClick = { onTabSelected("tables") }
                    )
                }

                if (activeMode.hasStaffAssignment) {
                    NavButton(
                        icon = Icons.Default.Calculate,
                        label = "STAFF",
                        isSelected = activeTab == "staff",
                        onClick = { onTabSelected("staff") }
                    )
                }
                
                NavButton(
                    icon = Icons.Default.Inventory,
                    label = "STOCK",
                    isSelected = activeTab == "inventory",
                    onClick = { onTabSelected("inventory") }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleSettings(true) },
                    modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                }
                
                IconButton(
                    onClick = { onLock() },
                    modifier = Modifier.size(56.dp).background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tint: Color? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) Color(0xFF0F172A) else Color.Transparent)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Color.White else (tint ?: Color(0xFF94A3B8))
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = if (isSelected) Color.White else (tint ?: Color(0xFF94A3B8)),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}
