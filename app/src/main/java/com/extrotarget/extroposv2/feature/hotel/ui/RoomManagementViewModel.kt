package com.extrotarget.extroposv2.feature.hotel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.hotel.Room
import com.extrotarget.extroposv2.core.data.model.hotel.RoomStatus
import com.extrotarget.extroposv2.feature.hotel.data.HotelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoomManagementViewModel @Inject constructor(
    private val repository: HotelRepository
) : ViewModel() {

    val rooms: StateFlow<List<Room>> = repository.getAllRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRoom(name: String, type: String, price: BigDecimal) {
        viewModelScope.launch {
            val room = Room(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                basePrice = price,
                status = RoomStatus.AVAILABLE
            )
            repository.addRoom(room)
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            repository.addRoom(room) // addRoom uses insertRoom with REPLACE
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            // repository.deleteRoom(room) // Need to add this to repository and DAO if needed
        }
    }
}
