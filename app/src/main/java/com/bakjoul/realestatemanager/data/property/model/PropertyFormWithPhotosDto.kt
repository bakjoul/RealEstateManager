package com.bakjoul.realestatemanager.data.property.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bakjoul.realestatemanager.data.photos.model.PhotoDraftDto

data class PropertyFormWithPhotosDto(
    @Embedded
    val propertyFormDto: PropertyFormDto,
    @Relation(
        parentColumn = "id",
        entityColumn = "property_form_id"
    )
    val photosDrafts: List<PhotoDraftDto>
)
