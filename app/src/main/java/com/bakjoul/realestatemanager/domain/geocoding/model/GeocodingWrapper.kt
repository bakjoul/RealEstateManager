package com.bakjoul.realestatemanager.domain.geocoding.model

import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import com.bakjoul.realestatemanager.domain.property.search.model.SearchLocationEntity

sealed class GeocodingWrapper {
    data class PropertyAddressSuccess(val result: PropertyAddressEntity) : GeocodingWrapper()
    data class SearchLocationSuccess(val result: SearchLocationEntity) : GeocodingWrapper()
    object NoResults : GeocodingWrapper()
    data class Failure(val message: String) : GeocodingWrapper()
    data class Error(val exception: Exception) : GeocodingWrapper()
}
