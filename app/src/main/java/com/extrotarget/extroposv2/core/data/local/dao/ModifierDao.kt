package com.extrotarget.extroposv2.core.data.local.dao

import androidx.room.*
import com.extrotarget.extroposv2.core.data.model.Modifier
import com.extrotarget.extroposv2.core.data.model.ModifierLink
import com.extrotarget.extroposv2.core.data.model.ModifierTargetType
import kotlinx.coroutines.flow.Flow

@Dao
interface ModifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModifier(modifier: Modifier)

    @Update
    suspend fun updateModifier(modifier: Modifier)

    @Query("UPDATE modifiers SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateModifierAvailability(id: String, isAvailable: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModifierLink(link: ModifierLink)

    @Query("SELECT * FROM modifiers")
    fun getAllModifiers(): Flow<List<Modifier>>

    @Query("""
        SELECT m.* FROM modifiers m
        INNER JOIN modifier_links ml ON m.id = ml.modifierId
        WHERE ml.targetId = :targetId AND ml.targetType = :targetType
    """)
    suspend fun getModifiersForTarget(targetId: String, targetType: ModifierTargetType): List<Modifier>

    @Query("DELETE FROM modifier_links WHERE targetId = :targetId AND targetType = :targetType")
    suspend fun deleteLinksForTarget(targetId: String, targetType: ModifierTargetType)

    @Transaction
    suspend fun updateModifiersForTarget(targetId: String, targetType: ModifierTargetType, modifierIds: List<String>) {
        deleteLinksForTarget(targetId, targetType)
        modifierIds.forEach { modId ->
            insertModifierLink(ModifierLink(modifierId = modId, targetId = targetId, targetType = targetType))
        }
    }
}
