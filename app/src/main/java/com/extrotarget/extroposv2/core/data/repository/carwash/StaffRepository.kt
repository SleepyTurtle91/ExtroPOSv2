package com.extrotarget.extroposv2.core.data.repository.carwash

import com.extrotarget.extroposv2.core.data.local.dao.carwash.CommissionRecordDao
import com.extrotarget.extroposv2.core.data.local.dao.carwash.StaffDao
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val staffDao: StaffDao,
    private val commissionRecordDao: CommissionRecordDao
) {
    fun getAllActiveStaff(): Flow<List<Staff>> = staffDao.getAllActiveStaff()

    suspend fun getStaffById(id: String): Staff? = staffDao.getStaffById(id)

    suspend fun saveStaff(staff: Staff) = staffDao.insertStaff(staff)

    suspend fun deleteStaff(staff: Staff) = staffDao.deleteStaff(staff)

    fun getCommissionRecordsForStaff(staffId: String): Flow<List<CommissionRecord>> =
        commissionRecordDao.getCommissionRecordsForStaff(staffId)

    fun getTotalEarningsForStaff(staffId: String): Flow<BigDecimal?> =
        commissionRecordDao.getTotalEarningsForStaff(staffId)

    suspend fun addCommissionRecords(records: List<CommissionRecord>) =
        commissionRecordDao.insertCommissionRecords(records)
}