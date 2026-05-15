package com.extrotarget.extroposv2.feature.reporting.domain.usecase

import com.extrotarget.extroposv2.core.domain.model.reporting.SalesSummary
import com.extrotarget.extroposv2.feature.reporting.data.repository.ReportingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSalesSummaryUseCase @Inject constructor(
    private val repository: ReportingRepository
) {
    operator fun invoke(start: Long, end: Long): Flow<SalesSummary> =
        repository.getSalesSummary(start, end)
}
