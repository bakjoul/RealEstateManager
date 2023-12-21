package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import java.math.BigDecimal
import java.text.DecimalFormat

data class AddPropertyViewState(
    val propertyTypeEntity: PropertyTypeEntity?,
    val forSaleSince: String?,
    val dateOfSale: String?,
    val isSold: Boolean,
    val priceHint: String,
    val price: String?,
    val currencyFormat: DecimalFormat,
    val surfaceLabel: String,
    val surface: BigDecimal,
    val numberOfRooms: String,
    val numberOfBathrooms: String,
    val numberOfBedrooms: String,
    val addressPredictions: List<AddPropertySuggestionItemViewState>,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipcode: String?,
    val photos: List<PhotoListItemViewState>,
    val isTypeErrorVisible: Boolean,
    val forSaleSinceError: String?,
    val dateOfSaleError: String?,
    val priceError: String?,
    val isSurfaceErrorVisible: Boolean,
    val isRoomsErrorVisible: Boolean,
    val addressError: String?,
    val cityError: String?,
    val stateError: String?,
    val zipcodeError: String?,
    val descriptionError: String?,
)
