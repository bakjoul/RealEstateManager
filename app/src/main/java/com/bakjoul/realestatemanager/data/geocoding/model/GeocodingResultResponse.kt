package com.bakjoul.realestatemanager.data.geocoding.model

import com.google.gson.annotations.SerializedName

data class GeocodingResultResponse (
    @SerializedName("address_components") val addressComponents: List<AddressComponentResponse>?,
    @SerializedName("formatted_address") val formattedAddress: String?,
    @SerializedName("geometry") val geometry: GeometryResponse?,
    @SerializedName("place_id") val placeId: String?,
    @SerializedName("plus_code") val plusCode: PlusCodeResponse?,
    @SerializedName("types") val types: List<String>?
)
