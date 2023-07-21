package com.bakjoul.realestatemanager.data.autocomplete

import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.domain.autocomplete.AutocompleteRepository
import com.bakjoul.realestatemanager.domain.autocomplete.AutocompleteResponseWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutocompleteRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) : AutocompleteRepository {

    private companion object {
        private const val TYPE = "geocode"
    }

    override suspend fun getAddressPredictions(input: String): AutocompleteResponseWrapper {
        try {
            val response = googleApi.requestPlaceAutocomplete(
                url = "https://maps.googleapis.com/maps/api/place/autocomplete/json",
                input = input,
                type = TYPE,
                key = BuildConfig.MAPS_API_KEY
            )

            return if (response.status == "OK") {
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