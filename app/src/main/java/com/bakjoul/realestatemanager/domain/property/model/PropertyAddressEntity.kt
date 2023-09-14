package com.bakjoul.realestatemanager.domain.property.model

data class PropertyAddressEntity (
    val address: String,
    val apartment: String,
    val zipcode: String,
    val city: String,
    val state: String,
    val country: String
)
