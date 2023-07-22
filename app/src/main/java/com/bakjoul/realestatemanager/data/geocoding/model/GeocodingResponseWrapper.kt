package com.bakjoul.realestatemanager.data.geocoding.model

sealed class GeocodingResponseWrapper {
    data class Success(val geocodingResponse: GeocodingResponse) : GeocodingResponseWrapper()
    data class Failure(val message: String) : GeocodingResponseWrapper()
    data class Error(val throwable: Throwable) : GeocodingResponseWrapper()
}
