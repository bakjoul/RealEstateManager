package com.bakjoul.realestatemanager.data.geocoding

import androidx.collection.LruCache
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.geocoding.model.AddressComponentResponse
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponse
import com.bakjoul.realestatemanager.domain.geocoding.GeocodingRepository
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import com.bakjoul.realestatemanager.domain.property.search.model.SearchLocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) : GeocodingRepository {

    private val lruCache: LruCache<String, GeocodingWrapper> = LruCache(100)

    override suspend fun getAddressDetails(placeId: String, forCitiesOnly: Boolean): GeocodingWrapper = withContext(Dispatchers.IO) {
        lruCache.get(placeId) ?: try {
            val response = googleApi.getAddressDetails(placeId = placeId)

            when (response.status) {
                "OK" -> {
                    val result = mapResponse(response, forCitiesOnly)

                    if (result != null) {
                        if (forCitiesOnly)
                            GeocodingWrapper.SearchLocationSuccess(result as SearchLocationEntity)
                        else
                            GeocodingWrapper.PropertyAddressSuccess(result as PropertyAddressEntity)
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

    private fun mapResponse(response: GeocodingResponse?, forCitiesOnly: Boolean): Any? {
        val responseResult = response?.results?.firstOrNull() ?: return null

        val streetNumber = extractAddressComponent(responseResult.addressComponents, "street_number")
        val route = extractAddressComponent(responseResult.addressComponents, "route")
        val zipcode = extractAddressComponent(responseResult.addressComponents, "postal_code")
        val city = extractAddressComponent(responseResult.addressComponents, "locality")
        val administrativeAreaLevel1 = extractAddressComponent(responseResult.addressComponents, "administrative_area_level_1")
        val administrativeAreaLevel2 = extractAddressComponent(responseResult.addressComponents, "administrative_area_level_2")
        val country = extractAddressComponent(responseResult.addressComponents, "country")
        val latitude = responseResult.geometry?.location?.lat
        val longitude = responseResult.geometry?.location?.lng

        if (forCitiesOnly) {
            if (city != null
                && administrativeAreaLevel1 != null
                && country != null
                && latitude != null
                && longitude != null
            ) {
                return SearchLocationEntity(
                    zipcode = zipcode,
                    city = city,
                    administrativeAreaLevel1 = administrativeAreaLevel1,
                    administrativeAreaLevel2 = administrativeAreaLevel2,
                    country = country,
                    latitude = latitude,
                    longitude = longitude
                )
            } else {
                return null
            }
        } else {
            if (streetNumber != null
                && route != null
                && zipcode != null
                && city != null
                && administrativeAreaLevel1 != null
                && country != null
                && latitude != null
                && longitude != null
            ) {
                return PropertyAddressEntity(
                    streetNumber = streetNumber,
                    route = route,
                    complementaryAddress = null,
                    zipcode = zipcode,
                    city = city,
                    administrativeAreaLevel1 = administrativeAreaLevel1,
                    administrativeAreaLevel2 = administrativeAreaLevel2,
                    country = country,
                    latitude = latitude,
                    longitude = longitude
                )
            } else {
                return null
            }
        }
    }

    private fun extractAddressComponent(addressComponents: List<AddressComponentResponse>?, type: String): String? {
        return addressComponents?.firstOrNull { it.types?.contains(type) == true }?.longName
    }
}
