package com.extrotarget.extroposv2.feature.reporting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.domain.model.reporting.*
import com.extrotarget.extroposv2.feature.reporting.domain.ExportService
import com.extrotarget.extroposv2.feature.reporting.domain.usecase.*
import com.extrotarget.extroposv2.feature.reporting.data.repository.ReportingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportingViewModel @Inject constructor(
    private val getSalesSummaryUseCase: GetSalesSummaryUseCase,
    private val getCommissionReportUseCase: GetCommissionReportUseCase,
    private val getTaxComplianceUseCase: GetTaxComplianceUseCase,
    private val exportService: ExportService,
    private val repository: ReportingRepository,
) : ViewModel() {

    private val _startDate = MutableStateFlow(getStartOfToday())
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis())
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    val salesSummary: StateFlow<SalesSummary?> = combine(startDate, endDate) { start, end ->
        getSalesSummaryUseCase(start, end)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val productPerformance: StateFlow<List<ProductPerformance>> = combine(startDate, endDate) { start, end ->
        repository.getProductPerformance(start, end)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commissionReport: StateFlow<List<StaffCommission>> = combine(startDate, endDate) { start, end ->
        getCommissionReportUseCase(start, end)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taxCompliance: StateFlow<List<TaxBreakdownItem>> = combine(startDate, endDate) { start, end ->
        getTaxComplianceUseCase(start, end)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addonPerformance: StateFlow<List<AddonPerformance>> = combine(startDate, endDate) { start, end ->
        repository.getAddonPerformance(start, end)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val occupancyTrends: StateFlow<List<OccupancyTrend>> = combine(startDate, endDate) { start, end ->
        repository.getOccupancyTrends(start, end)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
    }

    fun exportTaxReport(outputStream: OutputStream) {
        exportService.exportTaxComplianceToCsv(outputStream, taxCompliance.value)
    }

    private fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
