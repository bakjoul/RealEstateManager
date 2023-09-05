package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.data.property.model.PropertyType

data class AddPropertyViewState(
    val propertyType: PropertyType?,
    val dateHint: String,
    val priceHint: String,
    val surfaceLabel: String,
    val surface: String,
    val numberOfRooms: String,
    val numberOfBathrooms: String,
    val numberOfBedrooms: String,
    val addressPredictions: List<AddPropertySuggestionItemViewState>,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipcode: String?,
    val photos: List<AddPropertyPhotoItemViewState>
)
