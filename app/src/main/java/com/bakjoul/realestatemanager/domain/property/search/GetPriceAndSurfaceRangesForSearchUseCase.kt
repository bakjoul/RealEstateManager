package com.bakjoul.realestatemanager.domain.property.search

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PriceAndSurfaceRangesEntity
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class GetPriceAndSurfaceRangesForSearchUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {

    suspend fun invoke(
        currency: AppCurrency,
        euroRate: Double,
        surfaceUnit: SurfaceUnit
    ): PriceAndSurfaceRangesEntity {
        val results = propertyRepository.getPriceAndSurfaceRanges()
        val minPrice: BigDecimal
        val maxPrice: BigDecimal
        val minSurface: BigDecimal
        val maxSurface: BigDecimal

        if (currency == AppCurrency.EUR) {
            minPrice = results.lowestPrice.divide(BigDecimal(euroRate), 0, RoundingMode.FLOOR)
            maxPrice = results.highestPrice.divide(BigDecimal(euroRate), 0, RoundingMode.CEILING)
        } else {
            minPrice = results.lowestPrice
            maxPrice = results.highestPrice
        }

        if (surfaceUnit == SurfaceUnit.FEET) {
            minSurface =
                results.smallestSurface.times(BigDecimal(3.28084)).setScale(0, RoundingMode.FLOOR)
            maxSurface =
                results.largestSurface.times(BigDecimal(3.28084)).setScale(0, RoundingMode.CEILING)
        } else {
            minSurface = results.smallestSurface
            maxSurface = results.largestSurface
        }

        return PriceAndSurfaceRangesEntity(minPrice, maxPrice, minSurface, maxSurface)
    }
}
