package com.bakjoul.realestatemanager.domain.property.model

data class PropertyAddressEntity (
    val streetNumber: String,
    val route: String,
    val complementaryAddress: String?,
    val zipcode: String,
    val city: String,
    val administrativeAreaLevel1: String,
    val administrativeAreaLevel2: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
