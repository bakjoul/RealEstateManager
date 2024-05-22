package com.bakjoul.realestatemanager.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.DistanceUnit
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.geocoding.GetAddressDetailsUseCase
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.model.PriceAndSurfaceRangesEntity
import com.bakjoul.realestatemanager.domain.property.search.GetPriceAndSurfaceRangesForSearchUseCase
import com.bakjoul.realestatemanager.domain.property.search.model.SearchLocationParam
import com.bakjoul.realestatemanager.domain.search.GetSearchParametersFlowUseCase
import com.bakjoul.realestatemanager.domain.search.SetSearchParametersUseCase
import com.bakjoul.realestatemanager.domain.search.model.SearchDurationUnit
import com.bakjoul.realestatemanager.domain.search.model.SearchParametersEntity
import com.bakjoul.realestatemanager.domain.search.model.SearchPoi
import com.bakjoul.realestatemanager.domain.search.model.SearchType
import com.bakjoul.realestatemanager.domain.settings.GetCurrentSettingsUseCase
import com.bakjoul.realestatemanager.ui.common.SuggestionItemViewState
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPriceHint
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurfaceLabel
import com.bakjoul.realestatemanager.ui.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getAddressPredictionsUseCase: GetAddressPredictionsUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getCurrentSettingsUseCase: GetCurrentSettingsUseCase,
    private val getPriceAndSurfaceRangesForSearchUseCase: GetPriceAndSurfaceRangesForSearchUseCase,
    private val getSearchParametersFlowUseCase: GetSearchParametersFlowUseCase,
    private val setSearchParametersUseCase: SetSearchParametersUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "SearchViewModel"
        private const val REQUEST_TYPE = "(cities)"
        private val LOCATION_INPUT_DELAY = 300.milliseconds
        private val RANGE_UPDATE_DELAY = 300.milliseconds
    }

    private val currentParametersMutableStateFlow: MutableStateFlow<SearchParametersEntity> = MutableStateFlow(SearchParametersEntity())
    private val currentLocationInputMutableStateFlow: MutableStateFlow<Pair<String, Boolean>?> = MutableStateFlow(null)
    private val locationPredictionsFlow: Flow<AutocompleteWrapper?> = currentLocationInputMutableStateFlow
        .transformLatest {locationData ->
            if (locationData == null) {
                emit(null)
            } else {
                val (input, fromUser) = locationData
                if (fromUser) {
                    if (input.length < 2) {
                        emit(null)
                    } else {
                        delay(LOCATION_INPUT_DELAY)
                        emit(getAddressPredictionsUseCase.invoke(input, REQUEST_TYPE))
                    }
                }
            }
        }
    private val selectedLocationParamMutableStateFlow: MutableStateFlow<SearchLocationParam?> = MutableStateFlow(SearchLocationParam())
    private val errorsMutableStateFlow: MutableStateFlow<SearchParametersErrors> = MutableStateFlow(SearchParametersErrors())

    private var priceRangeUpdateJob: Job? = null
    private var surfaceRangeUpdateJob: Job? = null
    private var isInitializing = true
    private lateinit var priceAndSurfaceRanges: PriceAndSurfaceRangesEntity
    private var isLocationTextCleared = false

    val viewStateLiveData: LiveData<SearchViewState> = liveData {
        combine(
            getCurrentSettingsUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke().currencyRateEntity.rate) },
            getSearchParametersFlowUseCase.invoke(),
            locationPredictionsFlow,
            selectedLocationParamMutableStateFlow,
            errorsMutableStateFlow
        ) { settings, euroRate, searchParams, locationPredictions, selectedLocation, errors ->
            if (isInitializing) {
                currentParametersMutableStateFlow.update { searchParams }
                priceAndSurfaceRanges = getPriceAndSurfaceRangesForSearchUseCase.invoke(settings.currency, euroRate, settings.surfaceUnit)
                isInitializing = false
            }

            emit(
                SearchViewState(
                    statusButtonResId = getStatusButtonResId(searchParams),
                    durationFromEntryOrSaleDate = searchParams.durationFromEntryOrSaleDate,
                    durationFromEntryOrSaleDateUnit = searchParams.durationFromEntryOrSaleDateUnit,
                    location = formatLocation(selectedLocation),
                    locationPredictions = mapLocationPredictions(locationPredictions),
                    locationRadiusLabel = formatLocationRadiusLabel(settings.distanceUnit),
                    locationRadius = searchParams.locationRadius,
                    types = searchParams.types ?: emptyList(),
                    currency = settings.currency,
                    priceLabel = formatPriceHint(settings.currency),
                    priceFrom =  priceAndSurfaceRanges.lowestPrice.toFloat(),
                    priceTo = priceAndSurfaceRanges.highestPrice.toFloat(),
                    minPrice = searchParams.minPrice?.toFloat(),
                    maxPrice = searchParams.maxPrice?.toFloat(),
                    priceLabelFormatter = if (settings.currency == AppCurrency.EUR) {
                        NativeText.Resource(R.string.search_price_range_label_formatter_euro)
                    } else {
                        NativeText.Resource(R.string.search_price_range_label_formatter_dollar)
                    },
                    minPriceHelperText = NativeText.Argument(R.string.search_min, priceAndSurfaceRanges.lowestPrice),
                    maxPriceHelperText = NativeText.Argument(R.string.search_max, priceAndSurfaceRanges.highestPrice),
                    surfaceLabel = formatSurfaceLabel(settings.surfaceUnit),
                    surfaceFrom = priceAndSurfaceRanges.smallestSurface.toFloat(),
                    surfaceTo = priceAndSurfaceRanges.largestSurface.toFloat(),
                    minSurface = searchParams.minSurface?.toFloat(),
                    maxSurface = searchParams.maxSurface?.toFloat(),
                    surfaceLabelFormatter = if (settings.surfaceUnit == SurfaceUnit.FEET) {
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
                    durationUnitError = errors.durationFromEntryOrSaleDateUnitError,
                    locationError = errors.locationError
                )
            )
        }.collect()
    }

    val viewActionLiveData: LiveData<Event<SearchViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.HideLocationSuggestions -> emit(Event(SearchViewAction.HideSuggestions))
                is To.Toast -> {
                    if (it.message == NativeText.Resource(R.string.toast_selected_address_details_error) ||
                        it.message == NativeText.Resource(R.string.toast_selected_address_details_failure) ||
                        it.message == NativeText.Resource(R.string.toast_selected_address_no_results)
                    ) {
                        emit(Event(SearchViewAction.ShowToast(it.message)))
                    }
                }
                else -> Unit
            }
        }
    }

    private fun getStatusButtonResId(searchParams: SearchParametersEntity) =
        when (searchParams.isSold) {
            false -> R.id.search_status_for_sale_Button
            true -> R.id.search_status_sold_Button
            null -> R.id.search_status_all_Button
        }

    private fun resetLocationField() {
        Log.d("test", "resetLocationField: ")
        selectedLocationParamMutableStateFlow.update { SearchLocationParam() }
        currentParametersMutableStateFlow.update {
            it.copy(selectedLocation = null)
        }
    }

    private fun formatLocation(location: SearchLocationParam?): String? {
        return if (location?.city != null && location.administrativeAreaLevel1 != null) {
            buildString {
                append(location.city)
                if (location.administrativeAreaLevel2 != null) {
                    append(", ")
                    append(location.administrativeAreaLevel2)
                }
                if (location.administrativeAreaLevel2 == null) {
                    append(", ")
                    append(location.administrativeAreaLevel1)
                }
            }
        } else {
            null
        }
    }

    private fun mapLocationPredictions(wrapper: AutocompleteWrapper?): List<SuggestionItemViewState> =
        (wrapper as? AutocompleteWrapper.Success)?.let {
            wrapper.predictions.map { predictionEntity ->
                SuggestionItemViewState(
                    id = predictionEntity.placeId,
                    description = predictionEntity.description,
                    onSuggestionClicked = EquatableCallback {
                        viewModelScope.launch {
                            when (val geocodingResult = getAddressDetailsUseCase.invoke(predictionEntity.placeId, true)) {
                                is GeocodingWrapper.Error -> {
                                    navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_selected_address_details_error)))
                                    Log.d(TAG, "Geocoding error: ${geocodingResult.exception.message}")
                                }

                                is GeocodingWrapper.Failure -> {
                                    navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_selected_address_details_failure)))
                                    Log.d(TAG, "Geocoding failure: ${geocodingResult.message}")
                                }

                                is GeocodingWrapper.NoResults -> {
                                    navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_selected_address_no_results)))
                                }

                                is GeocodingWrapper.SearchLocationSuccess -> {
                                    navigateUseCase.invoke(To.HideLocationSuggestions)

                                    currentLocationInputMutableStateFlow.update {
                                        it?.copy(
                                            first = predictionEntity.description,
                                            second = false
                                        )
                                    }
                                    val selectedLocation = SearchLocationParam(
                                        zipcode = geocodingResult.result.zipcode,
                                        city = geocodingResult.result.city,
                                        administrativeAreaLevel1 = geocodingResult.result.administrativeAreaLevel1,
                                        administrativeAreaLevel2 = geocodingResult.result.administrativeAreaLevel2,
                                        country = geocodingResult.result.country,
                                        latitude = geocodingResult.result.latitude,
                                        longitude = geocodingResult.result.longitude
                                    )
                                    selectedLocationParamMutableStateFlow.update { selectedLocation }
                                    currentParametersMutableStateFlow.update {
                                        it.copy(selectedLocation = selectedLocation)
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                )

            }
        } ?: emptyList()

    private fun formatLocationRadiusLabel(distanceUnit: DistanceUnit): NativeText {
        return NativeText.Argument(
            R.string.search_radius_label,
            NativeText.Resource(distanceUnit.unitSymbol)
        )
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

    fun onLocationChanged(location: String) {
        Log.d("test", "onLocationChanged: $location")
        errorsMutableStateFlow.update {
            it.copy(locationError = null)
        }

        if (isLocationTextCleared) {
            Log.d("test", "onLocationChanged: location text cleared")
            isLocationTextCleared = false
            resetLocationField()
            return
        }

        if (currentLocationInputMutableStateFlow.value?.first != location &&
            selectedLocationParamMutableStateFlow.value == SearchLocationParam()
        ) {
            Log.d("test", "onLocationChanged: location filter changed")
            currentLocationInputMutableStateFlow.value = location to true
        }

        if (currentParametersMutableStateFlow.value.selectedLocation != SearchLocationParam() &&
            formatLocation(selectedLocationParamMutableStateFlow.value) != location) {
            resetLocationField()
        }
    }

    fun onLocationTextCleared() {
        Log.d("test", "onLocationTextCleared: ")
        isLocationTextCleared = true
        currentLocationInputMutableStateFlow.value = null
    }

    fun onLocationRadiusChanged(radius: Float) {
        Log.d("test", "onLocationRadiusChanged: $radius")
        currentParametersMutableStateFlow.update {
            it.copy(locationRadius = radius)
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
        val poi = SearchPoi.values().find { it.chipResId == chipId } ?: return
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
        val durationFromEntryOrSaleDateUnitError: NativeText? = null,
        val locationError: NativeText? = null
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
