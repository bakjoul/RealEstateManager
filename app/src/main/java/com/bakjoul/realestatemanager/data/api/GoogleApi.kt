package com.bakjoul.realestatemanager.data.api

import com.bakjoul.realestatemanager.data.autocomplete.model.AutocompleteResponse
import com.bakjoul.realestatemanager.data.geocoding.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApi {

    @GET("place/autocomplete/json")
    suspend fun getAddressPredictions(
        @Query("input") input: String,
        @Query("type") type: String,
        @Query("key") key: String
    ): AutocompleteResponse

    @GET("geocode/json")
    suspend fun getAddressDetails(
        @Query("place_id") placeId: String,
        @Query("key") key: String
    ): GeocodingResponse
}
