package com.bakjoul.realestatemanager.domain.autocomplete

import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponse

sealed class AutocompleteResponseWrapper {
    data class Success(val autocompleteResponse: AutocompleteResponse) : AutocompleteResponseWrapper()
    data class Failure(val message: String) : AutocompleteResponseWrapper()
    data class Error(val throwable: Throwable) : AutocompleteResponseWrapper()
}