package com.bakjoul.realestatemanager.domain.autocomplete

import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper

interface AutocompleteRepository {

    suspend fun getAddressPredictions(input: String, type: String): AutocompleteWrapper
}
