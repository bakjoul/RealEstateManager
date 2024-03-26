package com.bakjoul.realestatemanager.domain.property

import android.util.Log
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class UpdatePropertyUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {

    private companion object {
        private const val TAG = "UpdatePropertyUseCase"
    }

    suspend fun invoke(propertyEntity: PropertyEntity): Int {
        val updatedProperty = propertyRepository.updateProperty(propertyEntity)
        return if (updatedProperty > 0) {
            Log.d(TAG, "Property ${propertyEntity.id} updated successfully")
            updatedProperty
        } else if (updatedProperty == 0) {
            Log.d(TAG, "Property ${propertyEntity.id} unchanged")
            0
        } else {
            Log.d(TAG, "Couldn't update property ${propertyEntity.id}")
            -1
        }
    }
}
