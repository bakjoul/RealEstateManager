package com.bakjoul.realestatemanager.data.autocomplete

import androidx.collection.LruCache
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponse
import com.bakjoul.realestatemanager.domain.autocomplete.AutocompleteRepository
import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutocompleteRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) : AutocompleteRepository {

    private companion object {
        private const val AUTOCOMPLETE_API_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
        private const val TYPE = "geocode"
    }

    private val lruCache: LruCache<String, AutocompleteResponse> = LruCache(500)

    override suspend fun getAddressPredictions(input: String): AutocompleteResponseWrapper {
        val existingResponse = lruCache.get(input)

        if (existingResponse != null) {
            return AutocompleteResponseWrapper.Success(existingResponse)
        } else {
            try {
                val response = googleApi.getAddressPredictions(
                    url = AUTOCOMPLETE_API_URL,
                    input = input,
                    type = TYPE,
                    key = BuildConfig.MAPS_API_KEY
                )

                return if (response.status == "OK") {
                    lruCache.put(input, response)
                    AutocompleteResponseWrapper.Success(response)
                } else {
                    val status = response.status ?: "Unknown error"
                    AutocompleteResponseWrapper.Failure(status)
                }
            } catch (e: Exception) {
                return AutocompleteResponseWrapper.Error(e)
            }
        }
    }
}
