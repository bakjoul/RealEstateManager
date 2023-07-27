package com.bakjoul.realestatemanager.domain.geocoding.model

data class GeometryEntity(
    val location: LocationEntity,
    val locationType: String?
)
