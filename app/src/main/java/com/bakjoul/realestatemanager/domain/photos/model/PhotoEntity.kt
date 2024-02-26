package com.bakjoul.realestatemanager.domain.photos.model

data class PhotoEntity(
    val id: Long,
    val propertyId: Long,
    val uri: String,
    val description: String
)
