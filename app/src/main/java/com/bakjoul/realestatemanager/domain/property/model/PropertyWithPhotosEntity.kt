package com.bakjoul.realestatemanager.domain.property.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bakjoul.realestatemanager.data.property.PropertyDtoEntity

data class PropertyWithPhotosEntity(
    @Embedded
    val propertyDtoEntity: PropertyDtoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "property_id"
    )
    val photos: List<PhotoEntity>
)
