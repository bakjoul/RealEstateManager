package com.bakjoul.realestatemanager.domain.add.model

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import java.math.BigDecimal
import java.time.LocalDate

data class AddPropertyFormEntity(
    val type: PropertyTypeEntity? = null,
    val isSold: Boolean = false,
    val forSaleSince: LocalDate? = null,
    val dateOfSale: LocalDate? = null,
    val price: BigDecimal = BigDecimal.ZERO,
    val surface: BigDecimal = BigDecimal.ZERO,
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val bedrooms: Int = 0,
    val pointsOfInterest: List<PropertyPoiEntity> = emptyList(),
    val autoCompleteAddress: AddPropertyAddressEntity? = null,
    val address: AddPropertyAddressEntity = AddPropertyAddressEntity(),
    val description: String? = null,
    val photos: List<PhotoEntity> = emptyList(),
    val agent: String? = null,
)
