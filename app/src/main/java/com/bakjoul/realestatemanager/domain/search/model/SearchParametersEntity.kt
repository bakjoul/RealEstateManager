package com.bakjoul.realestatemanager.domain.search.model

import com.bakjoul.realestatemanager.domain.property.search.model.SearchLocationParam
import java.math.BigDecimal

data class SearchParametersEntity(
    val isSold: Boolean? = null,
    val durationFromEntryOrSaleDate: Int? = null,
    val durationFromEntryOrSaleDateUnit: SearchDurationUnit? = null,
    val selectedLocation: SearchLocationParam? = null,
    val locationRadius: Float? = null,
    val types: List<SearchType>? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val minSurface: BigDecimal? = null,
    val maxSurface: BigDecimal? = null,
    val numberOfRooms: BigDecimal? = null,
    val numberOfBathrooms: BigDecimal? = null,
    val numberOfBedrooms: BigDecimal? = null,
    val pointsOfInterest: List<SearchPoi>? = null,
)
