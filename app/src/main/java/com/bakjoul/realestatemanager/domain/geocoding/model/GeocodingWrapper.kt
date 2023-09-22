package com.bakjoul.realestatemanager.domain.geocoding.model

import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity

sealed class GeocodingWrapper {
    data class Success(val result: PropertyAddressEntity) : GeocodingWrapper()
    object NoResults : GeocodingWrapper()
    data class Failure(val message: String) : GeocodingWrapper()
    data class Error(val exception: Exception) : GeocodingWrapper()
}
