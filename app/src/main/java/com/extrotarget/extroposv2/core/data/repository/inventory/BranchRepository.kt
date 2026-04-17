package com.extrotarget.extroposv2.core.data.repository.inventory

import com.extrotarget.extroposv2.core.data.local.dao.BranchDao
import com.extrotarget.extroposv2.core.data.model.inventory.Branch
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchRepository @Inject constructor(
    private val branchDao: BranchDao
) {
    fun getAllBranches(): Flow<List<Branch>> = branchDao.getAllBranches()

    suspend fun getHQBranch(): Branch? = branchDao.getHQBranch()

    suspend fun getBranchById(id: String): Branch? = branchDao.getBranchById(id)

    suspend fun saveBranch(branch: Branch) = branchDao.insertBranch(branch)

    suspend fun updateBranch(branch: Branch) = branchDao.updateBranch(branch)

    suspend fun deleteBranch(branch: Branch) = branchDao.deleteBranch(branch)
}
