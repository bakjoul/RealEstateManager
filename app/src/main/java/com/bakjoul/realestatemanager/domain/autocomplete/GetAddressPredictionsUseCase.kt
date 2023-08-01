package com.bakjoul.realestatemanager.domain.autocomplete

import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import javax.inject.Inject

class GetAddressPredictionsUseCase @Inject constructor(private val autocompleteRepository: AutocompleteRepository) {
    suspend fun invoke(input: String): AutocompleteWrapper = autocompleteRepository.getAddressPredictions(input)
}
