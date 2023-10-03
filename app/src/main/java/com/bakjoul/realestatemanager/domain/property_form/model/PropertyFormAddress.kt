package com.bakjoul.realestatemanager.domain.property_form.model

data class PropertyFormAddress(
    val streetNumber: String? = null,
    val route: String? = null,
    val complementaryAddress: String? = null,
    val zipcode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
