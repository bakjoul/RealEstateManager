package com.bakjoul.realestatemanager.data.api

import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface GoogleApi {

    @GET
    suspend fun requestPlaceAutocomplete(
        @Url url: String,
        @Query("input") input: String,
        @Query("type") type: String,
        @Query("key") key: String
    ): AutocompleteResponse
}