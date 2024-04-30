package com.bakjoul.realestatemanager.domain.property.model

import java.math.BigDecimal

data class PriceAndSurfaceRangesEntity(
    val lowestPrice: BigDecimal,
    val highestPrice: BigDecimal,
    val smallestSurface: BigDecimal,
    val largestSurface: BigDecimal
)
