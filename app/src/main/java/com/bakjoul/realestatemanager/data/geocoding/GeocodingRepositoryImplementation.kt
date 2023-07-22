package com.bakjoul.realestatemanager.data.geocoding

import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponseWrapper
import com.bakjoul.realestatemanager.domain.geocoding.GeocodingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) :
    GeocodingRepository {

    override suspend fun getAddressDetails(placeId: String): GeocodingResponseWrapper {
        TODO("Not yet implemented")
    }

}