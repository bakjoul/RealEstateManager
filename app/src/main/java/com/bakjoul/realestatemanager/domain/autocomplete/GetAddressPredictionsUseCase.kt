package com.bakjoul.realestatemanager.domain.autocomplete

import android.util.Log
import com.bakjoul.realestatemanager.data.autocomplete.model.PredictionResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAddressPredictionsUseCase @Inject constructor(private val autocompleteRepository: AutocompleteRepository) {

    private companion object {
        private const val TAG = "GetAddressPredictionsUC"
    }

    suspend fun invoke(input: String): Flow<List<PredictionResponse>> = flow {
        when (val responseWrapper = autocompleteRepository.getAddressPredictions(input)) {
            is AutocompleteResponseWrapper.Success -> emit(responseWrapper.autocompleteResponse.predictions ?: emptyList())
            is AutocompleteResponseWrapper.Failure -> Log.i(TAG, "Failed to get address predictions")
            is AutocompleteResponseWrapper.Error -> Log.e(TAG, "Error while getting address predictions: ${responseWrapper.throwable.message}")
        }
    }
}