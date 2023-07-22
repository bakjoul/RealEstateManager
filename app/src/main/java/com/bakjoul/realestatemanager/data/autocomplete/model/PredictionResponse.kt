package com.bakjoul.realestatemanager.data.autocomplete.model

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("description") val description: String?,
    @SerializedName("matched_substrings") val matchedSubstrings: List<MatchedSubstringResponse>?,
    @SerializedName("place_id") val placeId: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("structured_formatting") val structuredFormatting: StructuredFormattingResponse?,
    @SerializedName("terms") val terms: List<TermResponse>?,
    @SerializedName("types") val types: List<String>?
)
