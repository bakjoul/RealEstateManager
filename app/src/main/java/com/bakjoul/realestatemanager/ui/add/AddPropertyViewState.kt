package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.ui.utils.NativeText
import java.math.BigDecimal
import java.text.DecimalFormat

data class AddPropertyViewState(
    val propertyTypeEntity: PropertyTypeEntity?,
    val forSaleSince: NativeText?,
    val dateOfSale: NativeText?,
    val isSold: Boolean,
    val priceHint: NativeText,
    val price: String?,
    val currencyFormat: DecimalFormat,
    val surfaceLabel: NativeText,
    val surface: BigDecimal,
    val numberOfRooms: BigDecimal,
    val numberOfBathrooms: BigDecimal,
    val numberOfBedrooms: BigDecimal,
    val amenities: List<PropertyPoiEntity>,
    val addressPredictions: List<AddPropertySuggestionItemViewState>,
    val address: String?,
    val complementaryAddress: String?,
    val city: String?,
    val state: String?,
    val zipcode: String?,
    val description: String?,
    val photos: List<PhotoListItemViewState>,
    val isTypeErrorVisible: Boolean,
    val forSaleSinceError: NativeText?,
    val dateOfSaleError: NativeText?,
    val priceError: NativeText?,
    val isSurfaceErrorVisible: Boolean,
    val isRoomsErrorVisible: Boolean,
    val addressError: NativeText?,
    val cityError: NativeText?,
    val stateError: NativeText?,
    val zipcodeError: NativeText?,
    val descriptionError: NativeText?,
)
