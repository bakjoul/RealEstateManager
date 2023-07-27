package com.bakjoul.realestatemanager.data.autocomplete

import androidx.collection.LruCache
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponse
import com.bakjoul.realestatemanager.domain.autocomplete.AutocompleteRepository
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutocompleteRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) : AutocompleteRepository {

    private companion object {
        private const val AUTOCOMPLETE_API_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
        private const val TYPE = "geocode"
    }

    private val lruCache: LruCache<String, AutocompleteResponse> = LruCache(500)

    override suspend fun getAddressPredictions(input: String): AutocompleteWrapper {
        val existingResponse = lruCache.get(input)

        if (existingResponse != null) {
            val predictions = mapPredictions(existingResponse)

            return AutocompleteWrapper.Success(predictions)
        } else {
            try {
                val response = googleApi.getAddressPredictions(
                    url = AUTOCOMPLETE_API_URL,
                    input = input,
                    type = TYPE,
                    key = BuildConfig.MAPS_API_KEY
                )

                return when (response.status) {
                    "OK" -> {
                        lruCache.put(input, response)
                        val predictions = mapPredictions(response)

                        AutocompleteWrapper.Success(predictions)
                    }
                    "ZERO_RESULTS" -> {
                        lruCache.put(input, response)
                        val emptyPredictions = listOf(PredictionEntity(address = "No results found", placeId = ""))

                        AutocompleteWrapper.NoResults(emptyPredictions)
                    }
                    else -> {
                        val status = response.status ?: "Unknown error"
                        AutocompleteWrapper.Failure(status)
                    }
                }
            } catch (e: Exception) {
                return AutocompleteWrapper.Error(e)
            }
        }
    }

    private fun mapPredictions(existingResponse: AutocompleteResponse): List<PredictionEntity> {
        val filteredPredictions = existingResponse.predictions?.filter { predictionResponse ->
            !predictionResponse.description.isNullOrBlank() && !predictionResponse.placeId.isNullOrBlank()
        }

        val predictionEntities = filteredPredictions?.map { predictionResponse ->
            PredictionEntity(
                address = predictionResponse.description!!,
                placeId = predictionResponse.placeId!!
            )
        } ?: emptyList()
        return predictionEntities
    }
}
