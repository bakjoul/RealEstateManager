package com.bakjoul.realestatemanager.data.search.model

import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormAddress
import java.math.BigDecimal

data class SearchParams(
    val isSold: Boolean? = null,
    val durationFromEntryOrSaleDate: Int? = null,
    val durationFromEntryOrSaleDateUnit: SearchDurationUnit? = null,
    val autoCompleteAddress: PropertyFormAddress? = null,
    val regionCity: PropertyFormAddress? = null,
    val types: List<SearchType>? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val minSurface: BigDecimal? = null,
    val maxSurface: BigDecimal? = null,
    val rooms: BigDecimal? = null,
    val bathrooms: BigDecimal? = null,
    val bedrooms: BigDecimal? = null,
    val pointsOfInterest: List<SearchPoi>? = null,
)
