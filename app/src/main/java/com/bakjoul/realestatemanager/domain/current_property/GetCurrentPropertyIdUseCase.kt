package com.bakjoul.realestatemanager.domain.current_property

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetCurrentPropertyIdUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
/*    fun invoke(): Flow<Long> {
        return currentPropertyIdRepository.getCurrentPropertyId()
    }*/
    fun invoke(): Flow<Long> = currentPropertyIdRepository.getCurrentPropertyId().onEach { propertyId ->
        Log.d("test", "Current Property ID: $propertyId")
    }
}
