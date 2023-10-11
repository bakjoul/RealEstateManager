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
        val updatedPropertyId = propertyRepository.updatePropertyDraft(propertyId, propertyForm)
        return if (updatedPropertyId > 0) {
            Log.d(TAG, "Property draft $updatedPropertyId updated successfully")
            updatedPropertyId
        } else if (updatedPropertyId == 0) {
            Log.d(TAG, "Property draft $propertyId unchanged")
            0
        } else {
            Log.d(TAG, "Couldn't update property draft $propertyId")
            -1
        }
    }
}
