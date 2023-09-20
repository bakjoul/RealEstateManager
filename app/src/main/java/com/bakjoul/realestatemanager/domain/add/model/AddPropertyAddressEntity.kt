package com.bakjoul.realestatemanager.domain.add.model

data class AddPropertyAddressEntity (
    val address: String? = null,
    val complementaryAddress: String? = null,
    val zipcode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null
)
