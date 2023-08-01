package com.bakjoul.realestatemanager.data.autocomplete

import androidx.collection.LruCache
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponse
import com.bakjoul.realestatemanager.domain.autocomplete.AutocompleteRepository
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutocompleteRepositoryImplementation @Inject constructor(private val googleApi: GoogleApi) : AutocompleteRepository {

    private companion object {
        private const val TYPE = "geocode"
    }

    private val lruCache: LruCache<String, AutocompleteWrapper> = LruCache(500)

    override suspend fun getAddressPredictions(input: String): AutocompleteWrapper = withContext(Dispatchers.IO) {
        lruCache.get(input) ?: try {
            val response = googleApi.getAddressPredictions(
                input = input,
                type = TYPE,
                key = BuildConfig.MAPS_API_KEY
            )

            when (response.status) {
                "OK" -> {
                    val predictions = mapPredictions(response)

                    if (predictions != null) {
                        AutocompleteWrapper.Success(predictions)
                    } else {
                        AutocompleteWrapper.NoResults
                    }.also { lruCache.put(input, it) }
                }
                "ZERO_RESULTS" -> AutocompleteWrapper.NoResults.also { lruCache.put(input, it) }
                else -> AutocompleteWrapper.Failure(response.status ?: "Unknown error")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            AutocompleteWrapper.Error(e)
        }
    }

    private fun mapPredictions(response: AutocompleteResponse): List<PredictionEntity>? =
        response.predictions?.mapNotNull { predictionResponse ->
            if (predictionResponse.description.isNullOrBlank() || predictionResponse.placeId.isNullOrBlank()) {
                return@mapNotNull null
            } else {
                PredictionEntity(
                    address = predictionResponse.description,
                    placeId = predictionResponse.placeId,
                )
            }
        }
}
