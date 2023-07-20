package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.data.property.PropertyType

data class AddPropertyViewState(
    val propertyType: PropertyType?,
    val dateHint: String,
    val priceHint: String,
    val surfaceLabel: String,
    val surface: String,
    val numberOfRooms: String,
    val numberOfBathrooms: String,
    val numberOfBedrooms: String
)