package com.extrotarget.extroposv2.core.data.repository.loyalty

import com.extrotarget.extroposv2.core.data.local.dao.loyalty.LoyaltyDao
import com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyConfig
import com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyPointTransaction
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyRepository @Inject constructor(
    private val loyaltyDao: LoyaltyDao
) {
    fun getAllMembers(): Flow<List<Member>> = loyaltyDao.getAllMembers()

    suspend fun getMemberByPhone(phoneNumber: String): Member? = loyaltyDao.getMemberByPhone(phoneNumber)

    suspend fun getMemberById(id: String): Member? = loyaltyDao.getMemberById(id)

    suspend fun saveMember(member: Member) = loyaltyDao.insertMember(member)

    suspend fun updateMember(member: Member) = loyaltyDao.updateMember(member)

    suspend fun addPoints(memberId: String, points: BigDecimal, saleId: String? = null, note: String? = null) =
        loyaltyDao.addPoints(memberId, points, saleId, note)

    suspend fun redeemPoints(memberId: String, points: BigDecimal, saleId: String? = null, note: String? = null) =
        loyaltyDao.redeemPoints(memberId, points, saleId, note)

    fun getConfig(): Flow<LoyaltyConfig?> = loyaltyDao.getConfig()

    suspend fun saveConfig(config: LoyaltyConfig) = loyaltyDao.saveConfig(config)

    fun getTransactionsForMember(memberId: String): Flow<List<LoyaltyPointTransaction>> =
        loyaltyDao.getTransactionsForMember(memberId)

    fun getSalesForMember(memberId: String): Flow<List<com.extrotarget.extroposv2.core.data.model.Sale>> =
        loyaltyDao.getSalesForMember(memberId)
}
