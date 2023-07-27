package com.bakjoul.realestatemanager.data.geocoding

import android.util.Log
import androidx.collection.LruCache
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.geocoding.model.AddressComponentResponse
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponse
import com.bakjoul.realestatemanager.data.geocoding.model.GeometryResponse
import com.bakjoul.realestatemanager.domain.geocoding.GeocodingRepository
import com.bakjoul.realestatemanager.domain.geocoding.model.AddressComponentEntity
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingResultEntity
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.geocoding.model.GeometryEntity
import com.bakjoul.realestatemanager.domain.geocoding.model.LocationEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) :
    GeocodingRepository {

    private companion object {
        private const val GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json"
    }

    private val lruCache: LruCache<String, GeocodingResponse> = LruCache(100)

    override suspend fun getAddressDetails(placeId: String): GeocodingWrapper {
        val existingResponse = lruCache.get(placeId)

        if (existingResponse != null) {
            val result = mapResults(existingResponse)

            return GeocodingWrapper.Success(result)
        } else {
            try {
                val response = googleApi.getAddressDetails(
                    url = GEOCODING_API_URL,
                    placeId = placeId,
                    key = BuildConfig.MAPS_API_KEY
                )

                return when (response.status) {
                    "OK" -> {
                        Log.d("test", "getAddressDetails: SALUT OK")
                        lruCache.put(placeId, response)
                        val result = mapResults(response)

                        GeocodingWrapper.Success(result)
                    }
                    "ZERO_RESULTS" -> {
                        Log.d("test", "getAddressDetails: SALUT ZERO")
                        lruCache.put(placeId, response)

                        GeocodingWrapper.NoResults(emptyList<GeocodingResultEntity>())
                    }
                    else -> {
                        Log.d("test", "getAddressDetails: SALUT ELSE")
                        val status = response.status ?: "Unknown error"
                        GeocodingWrapper.Failure(status)
                    }
                }
            } catch (e: Exception) {
                return GeocodingWrapper.Error(e)
            }
        }
    }

    private fun mapResults(response: GeocodingResponse): List<GeocodingResultEntity> {
        val responseResult = response.results?.firstOrNull() ?: return emptyList()

        val addressComponents = mapAddressComponents(responseResult.addressComponents)
        val geometry = mapGeometry(responseResult.geometry)
        val placeId = responseResult.placeId

        if (addressComponents.isEmpty() || geometry == null || placeId == null) {
            return emptyList()
        }

        return listOf(GeocodingResultEntity(addressComponents, geometry, placeId))
    }

    private fun mapAddressComponents(addressComponents: List<AddressComponentResponse>?): List<AddressComponentEntity> {
        val addressComponentEntities = mutableListOf<AddressComponentEntity>()

        addressComponents?.forEach { addressComponentResponse ->
            if (addressComponentResponse.longName != null
                && addressComponentResponse.shortName != null
                && addressComponentResponse.types != null
            ) {

                val addressComponentEntity = AddressComponentEntity(
                    addressComponentResponse.longName,
                    addressComponentResponse.shortName,
                    addressComponentResponse.types
                )

                addressComponentEntities.add(addressComponentEntity)
            }

        }

        return addressComponentEntities
    }

    private fun mapGeometry(geometry: GeometryResponse?): GeometryEntity? {
        if (geometry?.location?.lat == null || geometry.location.lng == null) {
            return null
        }

        val locationEntity = LocationEntity(
            geometry.location.lat,
            geometry.location.lng
        )

        return GeometryEntity(
            location = locationEntity,
            locationType = geometry.locationType,
        )
    }
}
