package com.extrotarget.extroposv2.ui.carwash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import com.extrotarget.extroposv2.ui.carwash.viewmodel.CarWashViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarWashJobQueueScreen(
    viewModel: CarWashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedJobForStaff by remember { mutableStateOf<CarWashJob?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Car Wash Job Queue") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1: QUEUED
                QueueColumn(
                    title = "QUEUED",
                    jobs = uiState.queuedJobs,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) { job ->
                    JobCard(job, onAction = { 
                        selectedJobForStaff = job
                    }, actionLabel = "START WASH")
                }

                // Column 2: IN PROGRESS
                QueueColumn(
                    title = "IN PROGRESS",
                    jobs = uiState.inProgressJobs,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) { job ->
                    JobCard(job, onAction = { 
                        viewModel.updateJobStatus(job.id, CarWashStatus.COMPLETED) 
                    }, actionLabel = "COMPLETE", isProcessing = true)
                }

                // Column 3: READY / COMPLETED
                QueueColumn(
                    title = "READY FOR PICKUP",
                    jobs = uiState.completedJobs,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ) { job ->
                    JobCard(job, onAction = { /* Archive or deliver */ }, actionLabel = "DELIVERED", isDone = true)
                }
            }
        }
    }

    if (selectedJobForStaff != null) {
        AlertDialog(
            onDismissRequest = { selectedJobForStaff = null },
            title = { Text("Assign Staff for ${selectedJobForStaff!!.plateNumber}") },
            text = {
                Column {
                    if (uiState.staffList.isEmpty()) {
                        Text("No active staff available. Please add staff in settings.")
                    } else {
                        uiState.staffList.forEach { staff ->
                            ListItem(
                                headlineContent = { Text(staff.name) },
                                supportingContent = { Text(staff.role) },
                                modifier = Modifier.clickable {
                                    viewModel.assignStaff(selectedJobForStaff!!.id, staff.id, staff.name)
                                    selectedJobForStaff = null
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedJobForStaff = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QueueColumn(
    title: String,
    jobs: List<CarWashJob>,
    modifier: Modifier = Modifier,
    containerColor: Color,
    content: @Composable (CarWashJob) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "$title (${jobs.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            jobs.forEach { job ->
                content(job)
            }
        }
    }
}

@Composable
fun JobCard(
    job: CarWashJob,
    onAction: () -> Unit,
    actionLabel: String,
    isProcessing: Boolean = false,
    isDone: Boolean = false
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = job.plateNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = timeFormat.format(Date(job.startTime)),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Text(
                text = job.serviceName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (job.assignedStaffName != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = job.assignedStaffName, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isDone) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isProcessing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isProcessing) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel)
                }
            } else {
                Text(
                    "COMPLETED AT ${job.completionTime?.let { timeFormat.format(Date(it)) }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
