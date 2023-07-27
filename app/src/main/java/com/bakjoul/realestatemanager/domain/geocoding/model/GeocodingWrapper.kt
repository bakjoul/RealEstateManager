package com.bakjoul.realestatemanager.domain.geocoding.model

sealed class GeocodingWrapper {
    data class Success(val results: List<GeocodingResultEntity>) : GeocodingWrapper()
    data class NoResults(val results: List<GeocodingResultEntity>) : GeocodingWrapper()
    data class Failure(val message: String) : GeocodingWrapper()
    data class Error(val throwable: Throwable) : GeocodingWrapper()
}
