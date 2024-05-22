package com.bakjoul.realestatemanager.domain.property.search.model

data class SearchLocationEntity(
    val zipcode: String?,
    val city: String,
    val administrativeAreaLevel1: String,
    val administrativeAreaLevel2: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
