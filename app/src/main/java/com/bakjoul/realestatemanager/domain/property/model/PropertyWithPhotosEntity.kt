package com.bakjoul.realestatemanager.domain.property.model

import androidx.room.Embedded
import androidx.room.Relation

data class PropertyWithPhotosEntity(
    @Embedded
    val propertyEntity: PropertyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "property_id"
    )
    val photos: List<PhotoEntity>
)