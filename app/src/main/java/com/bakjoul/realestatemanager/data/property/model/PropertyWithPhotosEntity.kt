package com.bakjoul.realestatemanager.data.property.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bakjoul.realestatemanager.data.photos.model.PhotoDtoEntity

data class PropertyWithPhotosEntity(
    @Embedded
    val propertyDtoEntity: PropertyDtoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "property_id"
    )
    val photos: List<PhotoDtoEntity>
)

