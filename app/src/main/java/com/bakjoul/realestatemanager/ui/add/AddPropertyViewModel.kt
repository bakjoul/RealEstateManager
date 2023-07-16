package com.bakjoul.realestatemanager.ui.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddPropertyViewModel @Inject constructor() : ViewModel() {

    val numberOfRoomsLiveData : MutableLiveData<Int> = MutableLiveData(0)

    fun decrementRooms() {
        val currentValue = numberOfRoomsLiveData.value
        if (currentValue != null && currentValue > 0) {
            numberOfRoomsLiveData.value = currentValue - 1
        }
    }

    fun incrementRooms() {
        numberOfRoomsLiveData.value = numberOfRoomsLiveData.value?.plus(1)
    }

}