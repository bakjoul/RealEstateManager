package com.bakjoul.realestatemanager.domain.autocomplete

import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponseWrapper

interface AutocompleteRepository {

    suspend fun getAddressPredictions(input: String): AutocompleteResponseWrapper
}