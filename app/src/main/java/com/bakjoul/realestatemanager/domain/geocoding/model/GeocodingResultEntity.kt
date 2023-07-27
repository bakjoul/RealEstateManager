package com.bakjoul.realestatemanager.domain.geocoding.model

data class GeocodingResultEntity(
    val addressComponents: List<AddressComponentEntity>,
    val geometry: GeometryEntity,
    val placeId: String
)
