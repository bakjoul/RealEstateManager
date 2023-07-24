package com.bakjoul.realestatemanager.data.geocoding

import androidx.collection.LruCache
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponse
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponseWrapper
import com.bakjoul.realestatemanager.domain.geocoding.GeocodingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) :
    GeocodingRepository {

    private companion object {
        private const val GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json"
    }

    private val lruCache: LruCache<String, GeocodingResponse> = LruCache(100)

    override suspend fun getAddressDetails(placeId: String): GeocodingResponseWrapper {
        val existingResponse = lruCache.get(placeId)

        if (existingResponse != null) {
            return GeocodingResponseWrapper.Success(existingResponse)
        } else {
            try {
                val response = googleApi.getAddressDetails(
                    url = GEOCODING_API_URL,
                    placeId = placeId,
                    key = BuildConfig.MAPS_API_KEY
                )

                return if (response.status == "OK") {
                    lruCache.put(placeId, response)
                    GeocodingResponseWrapper.Success(response)
                } else {
                    val status = response.status ?: "Unknown error"
                    GeocodingResponseWrapper.Failure(status)
                }
            } catch (e: Exception) {
                return GeocodingResponseWrapper.Error(e)
            }
        }
    }

}
