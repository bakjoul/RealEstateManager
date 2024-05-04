package com.bakjoul.realestatemanager.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.property.model.PriceAndSurfaceRangesEntity
import com.bakjoul.realestatemanager.domain.property.search.GetPriceAndSurfaceRangesForSearchUseCase
import com.bakjoul.realestatemanager.domain.search.GetSearchParametersFlowUseCase
import com.bakjoul.realestatemanager.domain.search.SetSearchParametersUseCase
import com.bakjoul.realestatemanager.domain.search.model.SearchDurationUnit
import com.bakjoul.realestatemanager.domain.search.model.SearchParametersEntity
import com.bakjoul.realestatemanager.domain.search.model.SearchPoi
import com.bakjoul.realestatemanager.domain.search.model.SearchType
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPriceHint
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurfaceLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getPriceAndSurfaceRangesForSearchUseCase: GetPriceAndSurfaceRangesForSearchUseCase,
    private val getSearchParametersFlowUseCase: GetSearchParametersFlowUseCase,
    private val setSearchParametersUseCase: SetSearchParametersUseCase
) : ViewModel() {

    private companion object {
        private val RANGE_UPDATE_DELAY = 300.milliseconds
    }

    private val currentParametersMutableStateFlow: MutableStateFlow<SearchParametersEntity> = MutableStateFlow(SearchParametersEntity())
    private val errorsMutableStateFlow: MutableStateFlow<SearchParametersErrors> = MutableStateFlow(SearchParametersErrors())

    private var priceRangeUpdateJob: Job? = null
    private var surfaceRangeUpdateJob: Job? = null
    private var isInitializing = true
    private lateinit var priceAndSurfaceRanges: PriceAndSurfaceRangesEntity

    val viewStateLiveData: LiveData<SearchViewState> = liveData {
        combine(
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke().currencyRateEntity.rate) },
            getCurrentSurfaceUnitUseCase.invoke(),
            getSearchParametersFlowUseCase.invoke(),
            errorsMutableStateFlow
        ) { currency, euroRate, surfaceUnit, searchParams, errors ->
            if (isInitializing) {
                currentParametersMutableStateFlow.update { searchParams }
                priceAndSurfaceRanges = getPriceAndSurfaceRangesForSearchUseCase.invoke(currency, euroRate, surfaceUnit)
                isInitializing = false
            }

            emit(
                SearchViewState(
                    statusButtonResId = getStatusButtonResId(searchParams),
                    durationFromEntryOrSaleDate = searchParams.durationFromEntryOrSaleDate,
                    durationFromEntryOrSaleDateUnit = searchParams.durationFromEntryOrSaleDateUnit,
                    address = "",
                    types = searchParams.types ?: emptyList(),
                    currency = currency,
                    priceLabel = formatPriceHint(currency),
                    priceFrom =  priceAndSurfaceRanges.lowestPrice.toFloat(),
                    priceTo = priceAndSurfaceRanges.highestPrice.toFloat(),
                    minPrice = searchParams.minPrice?.toFloat(),
                    maxPrice = searchParams.maxPrice?.toFloat(),
                    priceLabelFormatter = if (currency == AppCurrency.EUR) {
                        NativeText.Resource(R.string.search_price_range_label_formatter_euro)
                    } else {
                        NativeText.Resource(R.string.search_price_range_label_formatter_dollar)
                    },
                    minPriceHelperText = NativeText.Argument(R.string.search_min, priceAndSurfaceRanges.lowestPrice),
                    maxPriceHelperText = NativeText.Argument(R.string.search_max, priceAndSurfaceRanges.highestPrice),
                    surfaceLabel = formatSurfaceLabel(surfaceUnit),
                    surfaceFrom = priceAndSurfaceRanges.smallestSurface.toFloat(),
                    surfaceTo = priceAndSurfaceRanges.largestSurface.toFloat(),
                    minSurface = searchParams.minSurface?.toFloat(),
                    maxSurface = searchParams.maxSurface?.toFloat(),
                    surfaceLabelFormatter = if (surfaceUnit == SurfaceUnit.FEET) {
                        NativeText.Resource(R.string.search_surface_range_label_formatter_feet)
                    } else {
                        NativeText.Resource(R.string.search_surface_range_label_formatter_meters)
                    },
                    minSurfaceHelperText = NativeText.Argument(R.string.search_min, priceAndSurfaceRanges.smallestSurface),
                    maxSurfaceHelperText = NativeText.Argument(R.string.search_max, priceAndSurfaceRanges.largestSurface),
                    numberOfRooms = searchParams.numberOfRooms ?: BigDecimal.ZERO,
                    numberOfBathrooms = searchParams.numberOfBathrooms ?: BigDecimal.ZERO,
                    numberOfBedrooms = searchParams.numberOfBedrooms ?: BigDecimal.ZERO,
                    amenities = searchParams.pointsOfInterest ?: emptyList(),
                    durationUnitError = errors.durationFromEntryOrSaleDateUnitError
                )
            )
        }.collect()
    }

    private fun getStatusButtonResId(searchParams: SearchParametersEntity) =
        when (searchParams.isSold) {
            false -> R.id.search_status_for_sale_Button
            true -> R.id.search_status_sold_Button
            null -> R.id.search_status_all_Button
        }

    fun onStatusChanged(buttonId: Int) {
        Log.d("test", "onStatusChanged: $buttonId")
        currentParametersMutableStateFlow.update {
            it.copy(isSold = when (buttonId) {
                R.id.search_status_for_sale_Button -> false
                R.id.search_status_sold_Button -> true
                else -> null
            })
        }
    }

    fun onDurationChanged(duration: Int?) {
        Log.d("test", "onDurationChanged: $duration")
        currentParametersMutableStateFlow.update {
            it.copy(durationFromEntryOrSaleDate = duration)
        }
    }

    fun onDurationUnitChanged(searchDurationUnit: SearchDurationUnit) {
        Log.d("test", "onDurationUnitChanged: $searchDurationUnit")
        currentParametersMutableStateFlow.update {
            it.copy(
                durationFromEntryOrSaleDateUnit = when (searchDurationUnit) {
                    SearchDurationUnit.WEEKS -> SearchDurationUnit.WEEKS
                    SearchDurationUnit.MONTHS -> SearchDurationUnit.MONTHS
                    SearchDurationUnit.YEARS -> SearchDurationUnit.YEARS
                }
            )
        }
    }

    fun onTypeChipCheckedChanged(chipId: Int, isChecked: Boolean) {
        val type = SearchType.values().find { it.chipResId == chipId } ?: return
        val currentList = currentParametersMutableStateFlow.value.types ?: emptyList()
        if (isChecked) {
            currentParametersMutableStateFlow.update {
                it.copy(types = currentList.plus(type))
            }
        } else {
            currentParametersMutableStateFlow.update {
                it.copy(types = currentList.minus(type))
            }
        }
        Log.d("test", "onTypeChipCheckedChanged: ${currentParametersMutableStateFlow.value.types}")
    }

    fun onPriceRangeChanged(priceRange: Pair<BigDecimal, BigDecimal>) {
        priceRangeUpdateJob?.cancel()
        priceRangeUpdateJob = viewModelScope.launch {
            delay(RANGE_UPDATE_DELAY)
            currentParametersMutableStateFlow.update {
                it.copy(minPrice = priceRange.first, maxPrice = priceRange.second)
            }
            Log.d("test", "onPriceRangeChanged: ${currentParametersMutableStateFlow.value.minPrice}, ${currentParametersMutableStateFlow.value.maxPrice}")
        }
    }

    fun onSurfaceRangeChanged(surfaceRange: Pair<BigDecimal, BigDecimal>) {
        surfaceRangeUpdateJob?.cancel()
        surfaceRangeUpdateJob = viewModelScope.launch {
            delay(RANGE_UPDATE_DELAY)
            currentParametersMutableStateFlow.update {
                it.copy(minSurface = surfaceRange.first, maxSurface = surfaceRange.second)
            }
            Log.d("test", "onSurfaceRangeChanged: ${currentParametersMutableStateFlow.value.minSurface}, ${currentParametersMutableStateFlow.value.maxSurface}")
        }
    }

    fun onRoomsCountChanged(rooms: BigDecimal) {
        Log.d("test", "onRoomsCountChanged: $rooms")
        currentParametersMutableStateFlow.update {
            it.copy(numberOfRooms = rooms)
        }
    }

    fun onBathroomsCountChanged(bathrooms: BigDecimal) {
        Log.d("test", "onBathroomsCountChanged: $bathrooms")
        currentParametersMutableStateFlow.update {
            it.copy(numberOfBathrooms = bathrooms)
        }
    }

    fun onBedroomsCountChanged(bedrooms: BigDecimal) {
        Log.d("test", "onBedroomsCountChanged: $bedrooms")
        currentParametersMutableStateFlow.update {
            it.copy(numberOfBedrooms = bedrooms)
        }
    }

    fun onPoiChipCheckedChanged(chipId: Int, isChecked: Boolean) {
        Log.d("test", "onPoiChipCheckedChanged: $chipId, $isChecked")
        val poi = SearchPoi.values().find { it.poiResId == chipId } ?: return
        val currentList = currentParametersMutableStateFlow.value.pointsOfInterest ?: emptyList()
        if (isChecked) {
            currentParametersMutableStateFlow.update {
                it.copy(pointsOfInterest = currentList.plus(poi))
            }
        } else {
            currentParametersMutableStateFlow.update {
                it.copy(pointsOfInterest = currentList.minus(poi))
            }
        }
        Log.d("test", "onPoiChipCheckedChanged: ${currentParametersMutableStateFlow.value.pointsOfInterest}")
    }

    fun onApplyButtonClicked() {
        if (isFormValid()) {
            viewModelScope.launch {
                setSearchParametersUseCase.invoke(currentParametersMutableStateFlow.value)
            }
        }
    }

    private fun isFormValid() : Boolean {
        var isFormValid = true

        if (currentParametersMutableStateFlow.value.durationFromEntryOrSaleDate != null &&
            currentParametersMutableStateFlow.value.durationFromEntryOrSaleDateUnit == null
        ) {
            errorsMutableStateFlow.update {
                it.copy(durationFromEntryOrSaleDateUnitError = NativeText.Resource(R.string.search_error_duration_unit))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(durationFromEntryOrSaleDateUnitError = null)
            }
        }

        return isFormValid
    }

    data class SearchParametersErrors(
        val durationFromEntryOrSaleDateUnitError: NativeText? = null
    )

    private fun BigDecimal.fromDollarsToEurosFloor(euroRate: Double): BigDecimal {
        return divide(BigDecimal(euroRate), 0, RoundingMode.FLOOR)
    }
    private fun BigDecimal.fromDollarsToEurosCeiling(euroRate: Double): BigDecimal {
        return divide(BigDecimal(euroRate), 0, RoundingMode.CEILING)
    }
    private fun BigDecimal.fromMetersSquaredToFeetSquaredFloor(): BigDecimal {
        return times(BigDecimal(3.28084)).setScale(0, RoundingMode.FLOOR)
    }
    private fun BigDecimal.fromMetersSquaredToFeetSquaredCeiling(): BigDecimal {
        return times(BigDecimal(3.28084)).setScale(0, RoundingMode.CEILING)
    }
}
