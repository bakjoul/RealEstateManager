package com.bakjoul.realestatemanager.ui.search

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.search.model.SearchDurationUnit
import com.bakjoul.realestatemanager.domain.search.model.SearchPoi
import com.bakjoul.realestatemanager.domain.search.model.SearchType
import com.bakjoul.realestatemanager.ui.common.SuggestionItemViewState
import com.bakjoul.realestatemanager.ui.utils.NativeText
import java.math.BigDecimal

data class SearchViewState(
    val statusButtonResId: Int,
    val durationFromEntryOrSaleDate: Int?,
    val durationFromEntryOrSaleDateUnit: SearchDurationUnit?,
    val location: String?,
    val locationPredictions: List<SuggestionItemViewState>,
    val locationRadiusLabel: NativeText,
    val locationRadius: Float?,
    val types: List<SearchType>,
    val currency: AppCurrency,
    val priceLabel: NativeText,
    val priceFrom: Float,
    val priceTo: Float,
    val minPrice: Float?,
    val maxPrice: Float?,
    val priceLabelFormatter: NativeText,
    val minPriceHelperText: NativeText,
    val maxPriceHelperText: NativeText,
    val surfaceLabel: NativeText,
    val surfaceFrom: Float,
    val surfaceTo: Float,
    val minSurface: Float?,
    val maxSurface: Float?,
    val surfaceLabelFormatter: NativeText,
    val minSurfaceHelperText: NativeText,
    val maxSurfaceHelperText: NativeText,
    val numberOfRooms: BigDecimal,
    val numberOfBathrooms: BigDecimal,
    val numberOfBedrooms: BigDecimal,
    val amenities: List<SearchPoi>,
    val durationUnitError: NativeText?,
    val locationError: NativeText?
)
