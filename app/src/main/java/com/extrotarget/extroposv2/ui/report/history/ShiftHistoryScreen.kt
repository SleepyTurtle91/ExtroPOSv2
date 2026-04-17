package com.extrotarget.extroposv2.ui.report.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.data.model.Shift
import com.extrotarget.extroposv2.core.data.repository.ShiftRepository
import com.extrotarget.extroposv2.core.domain.usecase.PrintReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ShiftHistoryViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val printReceiptUseCase: PrintReceiptUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShiftHistoryUiState())
    val uiState: StateFlow<ShiftHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            shiftRepository.getAllShifts().collect { shifts ->
                _uiState.update { it.copy(shifts = shifts, isLoading = false) }
            }
        }
    }

    fun selectShift(shift: Shift) {
        _uiState.update { it.copy(selectedShift = shift) }
    }

    fun printZReport(shift: Shift) {
        viewModelScope.launch {
            printReceiptUseCase.printZReport(shift)
        }
    }
}

data class ShiftHistoryUiState(
    val shifts: List<Shift> = emptyList(),
    val selectedShift: Shift? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftHistoryScreen(
    viewModel: ShiftHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shift_history)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.shifts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.shift_no_history), color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                // List of Shifts
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.shifts) { shift ->
                        ShiftListItem(
                            shift = shift,
                            isSelected = uiState.selectedShift?.id == shift.id,
                            onClick = { viewModel.selectShift(shift) }
                        )
                    }
                }

                // Shift Detail Panel
                Box(modifier = Modifier.weight(1.5f).fillMaxHeight().background(Color(0xFF1E293B))) {
                    uiState.selectedShift?.let { shift ->
                        ShiftDetailPanel(
                            shift = shift,
                            onPrint = { viewModel.printZReport(shift) }
                        )
                    } ?: run {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.1f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select a shift to view details", color = Color.White.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShiftListItem(shift: Shift, isSelected: Boolean, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val startTime = dateFormat.format(Date(shift.startTime))

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0EA5E9) else Color(0xFF334155)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Shift #${shift.id}",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = startTime,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (shift.isClosed) Color(0xFFEF4444) else Color(0xFF22C55E), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (shift.isClosed) stringResource(R.string.shift_closed) else stringResource(R.string.shift_active),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ShiftDetailPanel(shift: Shift, onPrint: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.shift_details),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (shift.isClosed) {
                Button(
                    onClick = onPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.shift_print_z_report))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        DetailRow(stringResource(R.string.shift_staff), shift.staffName)
        DetailRow(stringResource(R.string.shift_start_time), dateFormat.format(Date(shift.startTime)))
        shift.endTime?.let {
            DetailRow(stringResource(R.string.shift_end_time), dateFormat.format(Date(it)))
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

        DetailRow(stringResource(R.string.shift_start_float_label), "RM ${"%.2f".format(shift.startFloat)}")

        val totalSales = shift.totalCashSales.add(shift.totalOtherSales)
        DetailRow(stringResource(R.string.shift_total_sales), "RM ${"%.2f".format(totalSales)}")
        DetailRow(stringResource(R.string.shift_total_tax), "RM ${"%.2f".format(shift.totalTax)}")
        
        if (shift.isClosed) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            DetailRow(stringResource(R.string.shift_expected_cash), "RM ${"%.2f".format(shift.endExpectedCash ?: BigDecimal.ZERO)}")
            DetailRow(stringResource(R.string.shift_actual_cash), "RM ${"%.2f".format(shift.endActualCash ?: BigDecimal.ZERO)}")
            
            val diff = (shift.endActualCash ?: BigDecimal.ZERO).subtract(shift.endExpectedCash ?: BigDecimal.ZERO)
            val diffColor = when {
                diff > BigDecimal.ZERO -> Color(0xFF22C55E)
                diff < BigDecimal.ZERO -> Color(0xFFEF4444)
                else -> Color.White
            }
            
            DetailRow(
                label = stringResource(R.string.shift_difference),
                value = "RM ${"%.2f".format(diff)}",
                valueColor = diffColor
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f))
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}
