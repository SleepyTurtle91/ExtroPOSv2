package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.network.SyncStatus
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaleHeader(
    activeMode: BusinessMode,
    uiState: SalesUiState,
    currentTime: Date,
    syncStatus: SyncStatus,
    sessionManager: SessionManager,
    onOpenShift: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.White)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.header_extropos),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = Color(0xFF0F172A),
                letterSpacing = (-1).sp
            )
            
            Surface(
                color = activeMode.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, activeMode.color.copy(alpha = 0.2f))
            ) {
                Text(
                    activeMode.displayName.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = activeMode.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            if (activeMode.hasTables && uiState.selectedTable != null) {
                Surface(
                    color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.TableBar, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                        Text(
                            stringResource(R.string.header_table_label, uiState.selectedTable?.name ?: ""),
                            color = Color(0xFF3B82F6),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            SyncStatusIndicator(syncStatus)

            Surface(
                modifier = Modifier.size(36.dp).clickable { onOpenDrawer() },
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CreditCard, // Using CreditCard as a proxy for drawer if specialized icon not found, or standard Inbox
                        contentDescription = "Open Drawer",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    timeFormat.format(currentTime),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    dateFormat.format(currentTime).uppercase(),
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            
            VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFE2E8F0))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.clickable { onOpenShift() }
                ) {
                    Text(
                        currentUser?.name?.uppercase() ?: stringResource(R.string.role_administrator),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFF0F172A)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                        Text(
                            stringResource(R.string.status_online),
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = Color(0xFFF1F5F9),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusIndicator(status: SyncStatus) {
    val color = when (status) {
        SyncStatus.IDLE -> Color(0xFF64748B)
        SyncStatus.CONNECTING -> Color(0xFFF59E0B)
        SyncStatus.CONNECTED -> Color(0xFF10B981)
        SyncStatus.DISCONNECTED -> Color(0xFFEF4444)
        is SyncStatus.ERROR -> Color(0xFFEF4444)
    }
    
    val text = when (status) {
        SyncStatus.IDLE -> stringResource(R.string.status_idle)
        SyncStatus.CONNECTING -> stringResource(R.string.status_connecting)
        SyncStatus.CONNECTED -> stringResource(R.string.status_synced)
        SyncStatus.DISCONNECTED -> stringResource(R.string.status_offline)
        is SyncStatus.ERROR -> stringResource(R.string.status_error)
    }

    val icon = when (status) {
        SyncStatus.IDLE -> Icons.Default.CloudQueue
        SyncStatus.CONNECTING -> Icons.Default.CloudSync
        SyncStatus.CONNECTED -> Icons.Default.CloudDone
        SyncStatus.DISCONNECTED -> Icons.Default.CloudOff
        is SyncStatus.ERROR -> Icons.Default.SyncProblem
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}
