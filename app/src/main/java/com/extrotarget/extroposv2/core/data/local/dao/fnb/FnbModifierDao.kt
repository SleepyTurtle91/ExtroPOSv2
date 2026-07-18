package com.extrotarget.extroposv2.core.data.local.dao.fnb

import androidx.room.*
import com.extrotarget.extroposv2.domain.fnb.model.ModifierGroup
import com.extrotarget.extroposv2.domain.fnb.model.ModifierOption
import kotlinx.coroutines.flow.Flow

@Dao
interface FnbModifierDao {
    @Query("SELECT * FROM fnb_modifier_groups WHERE menuItemId = :menuItemId")
    suspend fun getGroupsForMenuItem(menuItemId: String): List<ModifierGroup>

    @Query("SELECT * FROM fnb_modifier_options WHERE groupId = :groupId")
    suspend fun getOptionsForGroup(groupId: String): List<ModifierOption>

    @Transaction
    @Query("SELECT * FROM fnb_modifier_groups WHERE menuItemId = :menuItemId")
    fun getGroupsWithOptionsForMenuItem(menuItemId: String): Flow<List<ModifierGroupWithOptions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ModifierGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOption(option: ModifierOption)

    @Delete
    suspend fun deleteGroup(group: ModifierGroup)

    @Delete
    suspend fun deleteOption(option: ModifierOption)
}

data class ModifierGroupWithOptions(
    @Embedded val group: ModifierGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val options: List<ModifierOption>
)
