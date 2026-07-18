package com.extrotarget.extroposv2.core.data.local.dao.fnb

import androidx.room.*
import com.extrotarget.extroposv2.domain.fnb.model.KitchenStation
import kotlinx.coroutines.flow.Flow

@Dao
interface FnbStationDao {
    @Query("SELECT * FROM fnb_kitchen_stations")
    fun getAllStations(): Flow<List<KitchenStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: KitchenStation)

    @Delete
    suspend fun deleteStation(station: KitchenStation)
}
