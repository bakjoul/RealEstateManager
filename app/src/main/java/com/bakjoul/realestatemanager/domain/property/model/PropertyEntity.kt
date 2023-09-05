package com.bakjoul.realestatemanager.domain.property.model

import com.bakjoul.realestatemanager.data.property.PropertyAddress
import com.bakjoul.realestatemanager.data.property.PropertyPoi
import java.math.BigDecimal
import java.time.LocalDate

data class PropertyEntity(
    val id: Long,
    val type: String,
    val entryDate: LocalDate,
    val saleDate: LocalDate?,
    val price: BigDecimal,
    val surface: Double,
    val rooms: Int,
    val bathrooms: Int,
    val bedrooms: Int,
    val amenities: List<PropertyPoi>,
    val fullAddress: PropertyAddress,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val photos: List<PhotoEntity>,
    val agent: String,
)
