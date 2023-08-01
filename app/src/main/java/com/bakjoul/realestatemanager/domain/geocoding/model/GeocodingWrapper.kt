package com.bakjoul.realestatemanager.domain.geocoding.model

sealed class GeocodingWrapper {
    data class Success(val results: List<GeocodingResultEntity>) : GeocodingWrapper()
    object NoResults : GeocodingWrapper()
    data class Failure(val message: String) : GeocodingWrapper()
    data class Error(val exception: Exception) : GeocodingWrapper()
}
