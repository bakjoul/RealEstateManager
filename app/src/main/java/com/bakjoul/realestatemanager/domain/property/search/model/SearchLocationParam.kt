package com.bakjoul.realestatemanager.domain.property.search.model

data class SearchLocationParam(
    val zipcode: String? = null,
    val city: String? = null,
    val administrativeAreaLevel1: String? = null,
    val administrativeAreaLevel2: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
