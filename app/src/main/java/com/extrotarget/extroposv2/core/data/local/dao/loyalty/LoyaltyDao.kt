package com.extrotarget.extroposv2.core.data.local.dao.loyalty

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyConfig
import com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyPointTransaction
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM members WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getMemberByPhone(phoneNumber: String): Member?

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: String): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Update
    suspend fun updateMember(member: Member)

    @Insert
    suspend fun insertTransaction(transaction: LoyaltyPointTransaction)

    @Query("UPDATE members SET totalPoints = totalPoints + :points, lifetimePointsEarned = lifetimePointsEarned + :points WHERE id = :memberId")
    suspend fun updateMemberPoints(memberId: String, points: BigDecimal)

    @Query("UPDATE members SET totalPoints = totalPoints + :points WHERE id = :memberId")
    suspend fun deductMemberPoints(memberId: String, points: BigDecimal)

    @Query("SELECT * FROM sales WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getSalesForMember(memberId: String): Flow<List<com.extrotarget.extroposv2.core.data.model.Sale>>

    @Transaction
    suspend fun addPoints(memberId: String, points: BigDecimal, saleId: String?, note: String?) {
        updateMemberPoints(memberId, points)
        updateMemberTier(memberId)
        insertTransaction(
            LoyaltyPointTransaction(
                id = java.util.UUID.randomUUID().toString(),
                memberId = memberId,
                saleId = saleId,
                points = points,
                type = "EARNED",
                note = note
            )
        )
    }

    @Transaction
    suspend fun redeemPoints(memberId: String, points: BigDecimal, saleId: String?, note: String?) {
        deductMemberPoints(memberId, points.negate())
        insertTransaction(
            LoyaltyPointTransaction(
                id = java.util.UUID.randomUUID().toString(),
                memberId = memberId,
                saleId = saleId,
                points = points.negate(),
                type = "REDEEMED",
                note = note
            )
        )
    }

    @Transaction
    suspend fun updateMemberTier(memberId: String) {
        val member = getMemberById(memberId) ?: return
        val config = getLoyaltyConfigSync() ?: return
        
        val newTier = when {
            member.lifetimePointsEarned >= config.goldThreshold -> "GOLD"
            member.lifetimePointsEarned >= config.silverThreshold -> "SILVER"
            else -> "BRONZE"
        }
        
        if (newTier != member.tier) {
            updateMember(member.copy(tier = newTier))
        }
    }

    @Query("SELECT * FROM loyalty_config WHERE id = 1")
    suspend fun getLoyaltyConfigSync(): LoyaltyConfig?

    @Query("SELECT * FROM loyalty_config WHERE id = 1")
    fun getConfig(): Flow<LoyaltyConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: LoyaltyConfig)

    @Query("SELECT * FROM loyalty_transactions WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getTransactionsForMember(memberId: String): Flow<List<LoyaltyPointTransaction>>
}
