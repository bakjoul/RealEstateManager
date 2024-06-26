package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PriceAndSurfaceRangesEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    suspend fun generateNewDraftId(): Long

    suspend fun addProperty(propertyEntity: PropertyEntity): Long?

    suspend fun updateProperty(propertyEntity: PropertyEntity): Int

    fun getPropertiesFlow(): Flow<List<PropertyEntity>>

    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    suspend fun deleteProperty(id: Long): Int

    suspend fun addPropertyDraft(propertyForm: PropertyFormEntity): Long?

    suspend fun updatePropertyDraft(propertyId: Long, propertyForm: PropertyFormEntity): Int

    suspend fun getPropertyDraftIds(): List<Long>

    suspend fun doesDraftExistForPropertyId(propertyId: Long): Boolean

    fun getPropertyDraftsFlow(): Flow<List<PropertyFormEntity>>

    suspend fun getPropertyDraftById(id: Long): PropertyFormEntity?

    suspend fun deletePropertyDraft(id: Long): Int?

    suspend fun getPriceAndSurfaceRanges(): PriceAndSurfaceRangesEntity
}
