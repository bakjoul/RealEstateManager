package com.bakjoul.realestatemanager.domain.property.model

import androidx.room.PrimaryKey

data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val propertyId: Long,
    val url: String,
    val description: String
)
