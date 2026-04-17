package com.extrotarget.extroposv2.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.auth.SessionManager
import com.extrotarget.extroposv2.core.data.model.AdjustmentType
import com.extrotarget.extroposv2.core.data.model.Shift
import com.extrotarget.extroposv2.core.data.model.ShiftAdjustment
import com.extrotarget.extroposv2.core.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShiftManagementViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShiftUiState())
    val uiState: StateFlow<ShiftUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val adjustments: StateFlow<List<ShiftAdjustment>> = _uiState
        .flatMapLatest { state ->
            state.activeShift?.let { shiftRepository.getAdjustmentsForShift(it.id) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            shiftRepository.getActiveShift().collectLatest { shift ->
                _uiState.update { it.copy(activeShift = shift, isLoading = false) }
            }
        }
    }

    fun openShift(floatAmount: String) {
        viewModelScope.launch {
            val amount = try { BigDecimal(floatAmount) } catch (e: Exception) { BigDecimal.ZERO }
            val staff = sessionManager.getCurrentStaff()
            val newShift = Shift(
                staffId = staff?.id?.toString() ?: "0",
                staffName = staff?.name ?: "Unknown",
                startFloat = amount
            )
            shiftRepository.openShift(newShift)
        }
    }

    fun addAdjustment(amount: String, reason: String, type: AdjustmentType) {
        viewModelScope.launch {
            val shiftId = _uiState.value.activeShift?.id ?: return@launch
            val staff = sessionManager.getCurrentStaff()
            val adjustment = ShiftAdjustment(
                shiftId = shiftId,
                amount = try { BigDecimal(amount) } catch (e: Exception) { BigDecimal.ZERO },
                reason = reason,
                type = type,
                staffName = staff?.name ?: "Unknown"
            )
            shiftRepository.addAdjustment(adjustment)
        }
    }
}

data class ShiftUiState(
    val activeShift: Shift? = null,
    val isLoading: Boolean = true
)

@Composable
fun ShiftManagementScreen(
    viewModel: ShiftManagementViewModel = hiltViewModel(),
    onShiftOpened: () -> Unit,
    onViewActiveShift: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val adjustments by viewModel.adjustments.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (uiState.activeShift == null) {
            OpenShiftView(onOpenShift = { 
                viewModel.openShift(it)
                onShiftOpened()
            })
        } else {
            ActiveShiftManagementView(
                shift = uiState.activeShift!!,
                adjustments = adjustments,
                onAddAdjustment = viewModel::addAdjustment,
                onViewZReport = onViewActiveShift
            )
        }
    }
}

@Composable
fun ActiveShiftManagementView(
    shift: Shift,
    adjustments: List<ShiftAdjustment>,
    onAddAdjustment: (String, String, AdjustmentType) -> Unit,
    onViewZReport: () -> Unit
) {
    var showAdjustmentDialog by remember { mutableStateOf<AdjustmentType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.active_shift),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Started by ${shift.staffName} at ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(shift.startTime))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            Button(
                onClick = onViewZReport,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text(stringResource(R.string.close_shift_z_report))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdjustmentCard(
                title = stringResource(R.string.cash_in),
                amount = shift.cashIn,
                color = Color(0xFF10B981),
                icon = Icons.Default.Add,
                modifier = Modifier.weight(1f),
                onClick = { showAdjustmentDialog = AdjustmentType.CASH_IN }
            )
            AdjustmentCard(
                title = stringResource(R.string.cash_out),
                amount = shift.cashOut,
                color = Color(0xFFF59E0B),
                icon = Icons.Default.Remove,
                modifier = Modifier.weight(1f),
                onClick = { showAdjustmentDialog = AdjustmentType.CASH_OUT }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.recent_adjustments),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(adjustments) { adj ->
                    AdjustmentRow(adj)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }
                if (adjustments.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No adjustments recorded yet", color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }

    if (showAdjustmentDialog != null) {
        val currentType = showAdjustmentDialog!!
        AdjustmentDialog(
            type = currentType,
            onDismiss = { showAdjustmentDialog = null },
            onConfirm = { amount, reason ->
                onAddAdjustment(amount, reason, currentType)
                showAdjustmentDialog = null
            }
        )
    }
}

@Composable
fun AdjustmentCard(
    title: String,
    amount: BigDecimal,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    "RM ${String.format(Locale.getDefault(), "%.2f", amount)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdjustmentRow(adjustment: ShiftAdjustment) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(adjustment.reason, color = Color.White, fontWeight = FontWeight.Medium)
            Text(
                "${adjustment.staffName} • ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(adjustment.timestamp))}",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "${if (adjustment.type == AdjustmentType.CASH_IN) "+" else "-"} RM ${String.format(Locale.getDefault(), "%.2f", adjustment.amount)}",
            color = if (adjustment.type == AdjustmentType.CASH_IN) Color(0xFF10B981) else Color(0xFFF59E0B),
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentDialog(
    type: AdjustmentType,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == AdjustmentType.CASH_IN) "Cash In (Add to Drawer)" else "Cash Out (Remove from Drawer)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                    label = { Text("Amount (RM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (amount.isNotEmpty()) onConfirm(amount, reason) },
                enabled = amount.isNotEmpty() && reason.isNotEmpty()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenShiftView(onOpenShift: (String) -> Unit) {
    var floatAmount by remember { mutableStateOf("200.00") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(400.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.shift_open_new),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.shift_enter_float),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = floatAmount,
                    onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) floatAmount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.shift_start_float_label)) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onOpenShift(floatAmount) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.shift_start_btn), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
