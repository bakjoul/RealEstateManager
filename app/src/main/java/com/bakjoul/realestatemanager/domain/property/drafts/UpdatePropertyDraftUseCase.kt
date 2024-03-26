package com.bakjoul.realestatemanager.domain.property.drafts

import android.util.Log
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import javax.inject.Inject

class UpdatePropertyDraftUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {

    private companion object {
        private const val TAG = "UpdatePropertyDraftUC"
    }

    suspend fun invoke(propertyId: Long, propertyForm: PropertyFormEntity): Int {
        val updatedProperty = propertyRepository.updatePropertyDraft(propertyId, propertyForm)
        return if (updatedProperty > 0) {
            Log.d(TAG, "Property draft $propertyId updated successfully")
            updatedProperty
        } else if (updatedProperty == 0) {
            Log.d(TAG, "Property draft $propertyId unchanged")
            0
        } else {
            Log.d(TAG, "Couldn't update property draft $propertyId")
            -1
        }
    }
}
