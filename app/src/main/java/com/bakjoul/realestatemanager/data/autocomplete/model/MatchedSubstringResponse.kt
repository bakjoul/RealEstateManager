package com.bakjoul.realestatemanager.data.autocomplete.model

import com.google.gson.annotations.SerializedName

data class MatchedSubstringResponse (
    @SerializedName("length") val length: Int?,
    @SerializedName("offset") val offset: Int?
)
