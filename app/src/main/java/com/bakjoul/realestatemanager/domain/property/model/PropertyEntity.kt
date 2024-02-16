package com.bakjoul.realestatemanager.domain.property.model

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import java.math.BigDecimal
import java.time.LocalDate

data class PropertyEntity(
    val id: Long,
    val type: String,
    val forSaleSince: LocalDate,
    val saleDate: LocalDate?,
    val price: BigDecimal,
    val surface: BigDecimal,
    val rooms: BigDecimal,
    val bathrooms: BigDecimal,
    val bedrooms: BigDecimal,
    val amenities: List<PropertyPoiEntity>,
    val address: PropertyAddressEntity,
    val description: String,
    val photos: List<PhotoEntity>,
    val featuredPhotoId: Long?,
    val agent: String,
    val entryDate: LocalDate
)
