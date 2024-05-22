package com.bakjoul.realestatemanager.domain.autocomplete

import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import javax.inject.Inject

class GetAddressPredictionsUseCase @Inject constructor(private val autocompleteRepository: AutocompleteRepository) {
    suspend fun invoke(input: String, type: String): AutocompleteWrapper {
        return autocompleteRepository.getAddressPredictions(input, type)
    }
}
