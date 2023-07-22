package com.bakjoul.realestatemanager.domain.geocoding

import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponseWrapper

interface GeocodingRepository {

    suspend fun getAddressDetails(placeId: String): GeocodingResponseWrapper
}