package com.extrotarget.extroposv2.ui.carwash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import com.extrotarget.extroposv2.ui.carwash.viewmodel.CarWashViewModel
import com.extrotarget.extroposv2.ui.components.stitch.StitchKanbanColumn
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@Composable
fun CarWashJobQueueScreen(
    viewModel: CarWashViewModel
) {
    val jobs by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kanban Columns
            StitchKanbanColumn(
                title = "QUEUED",
                items = jobs.queuedJobs,
                statusColor = StitchColor.Outline,
                itemContent = { job -> CarWashJobStitchCard(job) },
                modifier = Modifier.weight(1f)
            )

            StitchKanbanColumn(
                title = "IN PROGRESS",
                items = jobs.inProgressJobs,
                statusColor = StitchColor.Primary,
                itemContent = { job -> CarWashJobStitchCard(job, isActive = true) },
                modifier = Modifier.weight(1f)
            )

            StitchKanbanColumn(
                title = "READY / SIAP",
                items = jobs.completedJobs,
                statusColor = StitchColor.Tertiary,
                itemContent = { job -> CarWashJobStitchCard(job) },
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom Dashboard: Staff Tracking
        Spacer(Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = StitchColor.SurfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchColor.OutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "STAFF COMMISSION TRACKER (TODAY)",
                    style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.OnSurfaceVariant)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StaffMiniCard("Ali", "12 Jobs", "RM 48.50", StitchColor.Primary)
                    StaffMiniCard("Abu", "8 Jobs", "RM 32.00", StitchColor.Tertiary)
                    StaffMiniCard("Chong", "5 Jobs", "RM 25.00", StitchColor.Secondary)
                }
            }
        }
    }
}

@Composable
private fun StaffMiniCard(name: String, count: String, earned: String, color: Color) {
    Surface(
        color = StitchColor.SurfaceContainerLow,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(180.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(color, CircleShape))
                Text(name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Jobs", style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp, color = StitchColor.Outline))
                    Text(count, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Earned", style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp, color = StitchColor.Outline))
                    Text(earned, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StitchColor.Primary))
                }
            }
        }
    }
}

@Composable
private fun CarWashJobStitchCard(job: CarWashJob, isActive: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) StitchColor.Primary else StitchColor.OutlineVariant),
        color = StitchColor.Surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    job.plateNumber.uppercase(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    "10:15 AM",
                    style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp, color = StitchColor.Outline)
                )
            }
            
            Text(
                job.carModel ?: "Standard Vehicle",
                style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = StitchColor.SurfaceContainerHigh,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        job.serviceName.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelCaps.copy(fontSize = 9.sp)
                    )
                }
                
                if (isActive) {
                    Text(
                        "00:14:22",
                        style = MaterialTheme.typography.labelCaps.copy(color = StitchColor.Primary),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
