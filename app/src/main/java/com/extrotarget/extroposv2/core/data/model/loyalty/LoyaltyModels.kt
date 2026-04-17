package com.extrotarget.extroposv2.core.data.model.loyalty

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "members")
data class Member(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String, // Unique identifier often used in POS
    val email: String? = null,
    val totalPoints: BigDecimal = BigDecimal.ZERO, // Current balance
    val lifetimePointsEarned: BigDecimal = BigDecimal.ZERO, // For tier calculation
    val joinDate: Long = System.currentTimeMillis(),
    val tier: String = "BRONZE", // BRONZE, SILVER, GOLD
    val status: String = "ACTIVE",
    val lastSyncTimestamp: Long = 0L
)

@Entity(tableName = "loyalty_transactions")
data class LoyaltyPointTransaction(
    @PrimaryKey val id: String,
    val memberId: String,
    val saleId: String?,
    val points: BigDecimal,
    val type: String, // EARNED, REDEEMED, ADJUSTED
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

@Entity(tableName = "loyalty_config")
data class LoyaltyConfig(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = true,
    val pointsPerCurrencyUnit: BigDecimal = BigDecimal.ONE, // RM 1 = 1 Point
    val redemptionValuePerPoint: BigDecimal = BigDecimal("0.01"), // 100 points = RM 1
    val minPointsToRedeem: BigDecimal = BigDecimal("100"),
    val silverThreshold: BigDecimal = BigDecimal("1000"), // Total points earned to reach Silver
    val goldThreshold: BigDecimal = BigDecimal("5000"),   // Total points earned to reach Gold
    val silverMultiplier: BigDecimal = BigDecimal("1.2"), // 20% bonus points
    val goldMultiplier: BigDecimal = BigDecimal("1.5")    // 50% bonus points
)

data class MemberWithHistory(
    val member: Member,
    val transactions: List<LoyaltyPointTransaction>,
    val sales: List<com.extrotarget.extroposv2.core.data.model.Sale>
)
