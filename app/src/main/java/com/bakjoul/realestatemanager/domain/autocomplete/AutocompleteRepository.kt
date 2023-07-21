package com.bakjoul.realestatemanager.domain.autocomplete

interface AutocompleteRepository {

    suspend fun getAddressPredictions(input: String): AutocompleteResponseWrapper
}