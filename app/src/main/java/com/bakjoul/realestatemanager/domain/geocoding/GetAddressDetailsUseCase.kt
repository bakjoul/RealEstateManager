package com.bakjoul.realestatemanager.domain.geocoding

import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import javax.inject.Inject

class GetAddressDetailsUseCase @Inject constructor(private val geocodingRepository: GeocodingRepository) {
    suspend fun invoke(placeId: String): GeocodingWrapper = geocodingRepository.getAddressDetails(placeId)
}
