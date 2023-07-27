package com.bakjoul.realestatemanager.domain.autocomplete.model

import java.lang.Exception

sealed class AutocompleteWrapper {
    data class Success(val predictions: List<PredictionEntity>) : AutocompleteWrapper()
    object NoResults : AutocompleteWrapper()
    data class Failure(val message: String) : AutocompleteWrapper()
    data class Error(val exception: Exception) : AutocompleteWrapper()
}
