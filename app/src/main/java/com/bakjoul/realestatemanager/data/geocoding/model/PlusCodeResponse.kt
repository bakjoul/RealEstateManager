package com.bakjoul.realestatemanager.data.geocoding.model

import com.google.gson.annotations.SerializedName

data class PlusCodeResponse(
    @SerializedName("compound_code") val compoundCode: String?,
    @SerializedName("global_code") val globalCode: String?
)
