package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.data.property.PropertyType

data class AddPropertyViewState(
    val propertyType: PropertyType?,
    val isForSale: Boolean,
    val dateHint: String,
    val surfaceHint: String,
    val numberOfRooms: Int,
    val numberOfBathrooms: Int,
    val numberOfBedrooms: Int
)