package com.bakjoul.realestatemanager.data.geocoding

import androidx.collection.LruCache
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.geocoding.model.AddressComponentResponse
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponse
import com.bakjoul.realestatemanager.domain.geocoding.GeocodingRepository
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) : GeocodingRepository {

    private val lruCache: LruCache<String, GeocodingWrapper> = LruCache(100)

    override suspend fun getAddressDetails(placeId: String): GeocodingWrapper = withContext(Dispatchers.IO) {
        lruCache.get(placeId) ?: try {
            val response = googleApi.getAddressDetails(placeId = placeId)

            when (response.status) {
                "OK" -> {
                    val result = mapResponse(response)

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

    private fun mapResponse(response: GeocodingResponse?): PropertyAddressEntity? {
        val responseResult = response?.results?.firstOrNull() ?: return null

        val streetNumber = extractAddressComponent(responseResult.addressComponents, "street_number")
        val route = extractAddressComponent(responseResult.addressComponents, "route")
        val zipcode = extractAddressComponent(responseResult.addressComponents, "postal_code")
        val city = extractAddressComponent(responseResult.addressComponents, "locality")
        val state = extractAddressComponent(responseResult.addressComponents, "administrative_area_level_1")
        val country = extractAddressComponent(responseResult.addressComponents, "country")
        val latitude = responseResult.geometry?.location?.lat
        val longitude = responseResult.geometry?.location?.lng

        if (streetNumber != null
            && route != null
            && zipcode != null
            && state != null
            && country != null
            && latitude != null
            && longitude != null
        ) {
            return PropertyAddressEntity(
                streetNumber = streetNumber,
                route = route,
                complementaryAddress = null,
                zipcode = zipcode,
                city = city ?: "",
                state = state,
                country = country,
                latitude = latitude,
                longitude = longitude
            )
        }

        return null
    }

    private fun extractAddressComponent(addressComponents: List<AddressComponentResponse>?, type: String): String? {
        return addressComponents?.firstOrNull { it.types?.contains(type) == true }?.longName
    }
}
