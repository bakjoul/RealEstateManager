package com.bakjoul.realestatemanager.domain.autocomplete

import android.util.Log
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAddressPredictionsUseCase @Inject constructor(private val autocompleteRepository: AutocompleteRepository) {

    private companion object {
        private const val TAG = "GetAddressPredictionsUC"
    }

    suspend fun invoke(input: String): Flow<List<PredictionEntity>> = flow {
        when (val wrapper = autocompleteRepository.getAddressPredictions(input)) {
            is AutocompleteWrapper.Success -> emit(wrapper.predictions)
            is AutocompleteWrapper.Empty -> emit(wrapper.predictions)
            is AutocompleteWrapper.Failure -> Log.i(TAG, "Failed to get address predictions")
            is AutocompleteWrapper.Error -> Log.e(TAG, "Error while getting address predictions: ${wrapper.throwable.message}")
        }
    }
}
