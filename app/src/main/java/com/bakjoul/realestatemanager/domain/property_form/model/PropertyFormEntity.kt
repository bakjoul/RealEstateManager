package com.bakjoul.realestatemanager.domain.property_form.model

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class PropertyFormEntity(
    val id: Long = 0,
    val type: PropertyTypeEntity? = null,
    val isSold: Boolean? = null,
    val forSaleSince: LocalDate? = null,
    val dateOfSale: LocalDate? = null,
    val referencePrice: BigDecimal? = null,
    val priceFromUser: BigDecimal? = null,
    val referenceSurface: BigDecimal? = null,
    val surfaceFromUser: BigDecimal? = null,
    val rooms: BigDecimal? = null,
    val bathrooms: BigDecimal? = null,
    val bedrooms: BigDecimal? = null,
    val pointsOfInterest: List<PropertyPoiEntity>? = null,
    val autoCompleteAddress: PropertyFormAddress? = null,
    val address: PropertyFormAddress? = null,
    val description: String? = null,
    val photos: List<PhotoEntity>? = null,
    val featuredPhotoId: Long? = null,
    val agent: String? = null,
    val lastUpdate: LocalDateTime,
)
