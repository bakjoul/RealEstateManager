package com.bakjoul.realestatemanager.data.geocoding

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) :
    GeocodingRepository {

    private val lruCache: LruCache<String, GeocodingWrapper> = LruCache(100)

    override suspend fun getAddressDetails(placeId: String): GeocodingWrapper = withContext(Dispatchers.IO) {
        lruCache.get(placeId) ?: try {
            val response = googleApi.getAddressDetails(
                placeId = placeId,
                key = BuildConfig.MAPS_API_KEY
            )

            when (response.status) {
                "OK" -> {
                    val result = mapResults(response)

                    if (result != null) {
                        GeocodingWrapper.Success(result)
                    } else {
                        GeocodingWrapper.NoResults
                    }.also { lruCache.put(placeId, it) }
                }
                "ZERO_RESULTS" -> GeocodingWrapper.NoResults.also { lruCache.put(placeId, it) }
                else -> GeocodingWrapper.Failure(response.status ?: "Unknown error")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            GeocodingWrapper.Error(e)
        }
    }

    private fun mapResults(response: GeocodingResponse?): List<GeocodingResultEntity>? {
        val responseResult = response?.results?.firstOrNull() ?: return null

        val addressComponents = mapAddressComponents(responseResult.addressComponents)
        val geometry = mapGeometry(responseResult.geometry)
        val placeId = responseResult.placeId

        if (addressComponents.isNotEmpty() && geometry != null && placeId != null) {
            return listOf(GeocodingResultEntity(addressComponents, geometry, placeId))
        }

        return null
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
