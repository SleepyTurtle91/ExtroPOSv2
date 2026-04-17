package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.inventory.Branch
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches")
    fun getAllBranches(): Flow<List<Branch>>

    @Query("SELECT * FROM branches WHERE isHQ = 1 LIMIT 1")
    suspend fun getHQBranch(): Branch?

    @Query("SELECT * FROM branches WHERE id = :id")
    suspend fun getBranchById(id: String): Branch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: Branch)

    @Update
    suspend fun updateBranch(branch: Branch)

    @Delete
    suspend fun deleteBranch(branch: Branch)
}
