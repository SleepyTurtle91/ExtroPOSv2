package com.extrotarget.extroposv2.feature.reporting.domain.usecase

import com.extrotarget.extroposv2.core.domain.model.reporting.TaxBreakdownItem
import com.extrotarget.extroposv2.feature.reporting.data.repository.ReportingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaxComplianceUseCase @Inject constructor(
    private val repository: ReportingRepository
) {
    operator fun invoke(start: Long, end: Long): Flow<List<TaxBreakdownItem>> =
        repository.getTaxComplianceBreakdown(start, end)
}
