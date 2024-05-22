package com.bakjoul.realestatemanager.domain.geocoding

import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper

interface GeocodingRepository {

    suspend fun getAddressDetails(placeId: String, forCitiesOnly: Boolean): GeocodingWrapper
}
