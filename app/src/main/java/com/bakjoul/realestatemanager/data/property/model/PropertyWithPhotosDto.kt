package com.bakjoul.realestatemanager.data.property.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto

data class PropertyWithPhotosDto(
    @Embedded
    val propertyDto: PropertyDto,
    @Relation(
        parentColumn = "id",
        entityColumn = "property_id"
    )
    val photos: List<PhotoDto>
)
