package com.bakjoul.realestatemanager.domain.geocoding.model

data class AddressComponentEntity(
    val longName: String,
    val shortName: String,
    val types: List<String>,
)
