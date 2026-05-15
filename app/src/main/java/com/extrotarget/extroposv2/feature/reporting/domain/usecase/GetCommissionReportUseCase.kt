package com.extrotarget.extroposv2.feature.reporting.domain.usecase

import com.extrotarget.extroposv2.core.domain.model.reporting.StaffCommission
import com.extrotarget.extroposv2.feature.reporting.data.repository.ReportingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommissionReportUseCase @Inject constructor(
    private val repository: ReportingRepository
) {
    operator fun invoke(start: Long, end: Long): Flow<List<StaffCommission>> =
        repository.getStaffCommissionReport(start, end)
}
