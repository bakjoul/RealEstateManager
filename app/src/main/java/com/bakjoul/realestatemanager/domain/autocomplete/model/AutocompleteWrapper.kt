package com.bakjoul.realestatemanager.domain.autocomplete.model

sealed class AutocompleteWrapper {
    data class Success(val predictions: List<PredictionEntity>) : AutocompleteWrapper()
    data class NoResults(val predictions: List<PredictionEntity>) : AutocompleteWrapper()
    data class Failure(val message: String) : AutocompleteWrapper()
    data class Error(val throwable: Throwable) : AutocompleteWrapper()
}
