package com.extrotarget.extroposv2.core.data.local.dao.carwash

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE isActive = 1")
    fun getAllActiveStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getStaffById(id: String): Staff?

    @Query("SELECT * FROM staff")
    suspend fun getAllStaff(): List<Staff>

    @Query("SELECT * FROM staff WHERE pin = :pin AND isActive = 1")
    suspend fun getStaffByPin(pin: String): Staff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff)

    @Update
    suspend fun updateStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)
}