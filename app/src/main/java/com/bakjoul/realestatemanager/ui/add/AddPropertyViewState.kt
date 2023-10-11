package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.EquatableCallbackWithTwoParams
import java.text.DecimalFormat

data class AddPropertyViewState(
    val propertyTypeEntity: PropertyTypeEntity?,
    val isSold: Boolean,
    val priceHint: String,
    val currencyFormat: DecimalFormat,
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
    val photos: List<PhotoListItemViewState>,
)
