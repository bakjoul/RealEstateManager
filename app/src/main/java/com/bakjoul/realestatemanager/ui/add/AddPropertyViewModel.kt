package com.bakjoul.realestatemanager.ui.add

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.property.PropertyType
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import com.bakjoul.realestatemanager.domain.geocoding.GetAddressDetailsUseCase
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingResultEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AddPropertyViewModel @Inject constructor(
    private val application: Application,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val getAddressPredictionsUseCase: GetAddressPredictionsUseCase,
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase
) : ViewModel() {

    private val propertyTypeMutableStateFlow: MutableStateFlow<PropertyType?> = MutableStateFlow(null)
    private val isForSaleMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val dateMutableStateFlow: MutableStateFlow<LocalDate?> = MutableStateFlow(null)
    private val surfaceMutableStateFlow: MutableStateFlow<Double> = MutableStateFlow(0.0)
    private val numberOfRoomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numberOfBathroomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numberOfBedroomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val currentAddressInputMutableStateFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    private val addressPredictionsFlow: Flow<AutocompleteWrapper?> = currentAddressInputMutableStateFlow
        .transformLatest { input ->
            if (input.isNullOrEmpty() || input.length < 5) {
                emit(null)
            } else {
                delay(300.milliseconds)
                emit(getAddressPredictionsUseCase.invoke(input))
            }
        }
    private val selectedAddressMutableStateFlow: MutableStateFlow<PredictionEntity?> = MutableStateFlow(null)
    private val selectedAddressDetailsFlow: Flow<GeocodingResultEntity?> = selectedAddressMutableStateFlow.flatMapLatest { prediction ->
        if (prediction == null) {
            flowOf(null)
        } else {
            getAddressDetailsUseCase.invoke(prediction.placeId)
        }
    }

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        combine(
            getCurrentCurrencyUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke(),
            propertyTypeMutableStateFlow,
            isForSaleMutableStateFlow,
            surfaceMutableStateFlow,
            numberOfRoomsMutableStateFlow,
            numberOfBathroomsMutableStateFlow,
            numberOfBedroomsMutableStateFlow,
            addressPredictionsFlow,
            selectedAddressDetailsFlow
        ) { currency, surfaceUnit, propertyType, isForSale, surface, numberOfRooms, numberOfBathrooms, numberOfBedrooms, address, addressDetails ->
            AddPropertyViewState(
                propertyType = propertyType,
                dateHint = formatDateHint(isForSale),
                priceHint = formatPriceHint(currency),
                surfaceLabel = formatSurfaceLabel(surfaceUnit),
                surface = formatSurfaceValue(surface),
                numberOfRooms = numberOfRooms.toString(),
                numberOfBathrooms = numberOfBathrooms.toString(),
                numberOfBedrooms = numberOfBedrooms.toString(),
                addressPredictions = mapAddressPredictions(address),
                address = formatAddress(addressDetails),
                state = "",
                city = "",
                zipcode = ""
            )
        }.collect {
            emit(it)
        }
    }

    private fun formatAddress(addressDetails: GeocodingResultEntity?): String? {
        return if (addressDetails == null) {
            null
        } else {
            val streetNumberComponent = addressDetails.addressComponents.find { it.types.contains("street_number") }
            val routeComponent = addressDetails.addressComponents.find { it.types.contains("route") }

            if (streetNumberComponent != null && routeComponent != null) {
                return streetNumberComponent.longName + " " + routeComponent.longName
            }

            return null
        }
    }

    private val _viewActionLiveData = MutableLiveData<Event<AddPropertyViewAction>>()
    val viewActionLiveData: LiveData<Event<AddPropertyViewAction>> get() = _viewActionLiveData

    private fun mapAddressPredictions(wrapper : AutocompleteWrapper?): List<AddPropertySuggestionItemViewState> = when (wrapper) {
        is AutocompleteWrapper.Error,
        is AutocompleteWrapper.Failure -> emptyList()
        is AutocompleteWrapper.NoResults -> listOf(
            AddPropertySuggestionItemViewState(
                "",
                "NO SUGGESTIONS",

            )
        )
        is AutocompleteWrapper.Success -> wrapper.predictions.map { predictionEntity ->
            AddPropertySuggestionItemViewState(
                id = predictionEntity.placeId,
                address = predictionEntity.address,
                onSuggestionClicked = EquatableCallback {
                    _viewActionLiveData.value = Event(AddPropertyViewAction.HideSuggestions)
                    selectedAddressMutableStateFlow.value = predictionEntity
                }
            )
        }
        null -> emptyList()
    }

    private fun formatSurfaceValue(surface: Double): String {
        return if (surface == surface.toInt().toDouble()) {
            surface.toInt().toString()
        } else {
            String.format("%.1f", surface)
        }
    }

    private fun formatPriceHint(currency: AppCurrency): String {
        return "Price (${currency.symbol})"
    }

    private fun formatDateHint(isForSale: Boolean): String {
        return if (isForSale) {
            application.getString(R.string.property_for_sale_since)
        } else {
            application.getString(R.string.property_sold_on)
        }
    }

    private fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): String {
        return "Surface (${surfaceUnit.unit})"
    }

    fun onPropertyTypeChanged(checkedId: Int) {
        propertyTypeMutableStateFlow.value = when (checkedId) {
            R.id.add_property_type_flat_RadioButton -> PropertyType.Flat
            R.id.add_property_type_house_RadioButton -> PropertyType.House
            R.id.add_property_type_duplex_RadioButton -> PropertyType.Duplex
            R.id.add_property_type_penthouse_RadioButton -> PropertyType.Penthouse
            R.id.add_property_type_loft_RadioButton -> PropertyType.Loft
            R.id.add_property_type_other_RadioButton -> PropertyType.Other
            else -> null
        }
    }

    fun onSaleStatusChanged(isForSale: Boolean) {
        isForSaleMutableStateFlow.value = isForSale
    }

    fun onSurfaceValueChanged(surface: Double) {
        if (surface >= 0.0) {
            surfaceMutableStateFlow.value = surface
        }
    }

    fun decrementSurface(surface: Double) {
        if (surface > 0) {
            surfaceMutableStateFlow.value = surface - 1
        }
    }

    fun incrementSurface(surface: Double) {
        surfaceMutableStateFlow.value = surface + 1
    }

    fun onRoomsValueChanged(rooms: Int) {
        if (rooms >= 0) {
            numberOfRoomsMutableStateFlow.value = rooms
        }
    }

    fun decrementRooms(rooms: Int) {
        if (rooms > 0) {
            numberOfRoomsMutableStateFlow.value = rooms - 1
        }
    }

    fun incrementRooms(rooms: Int) {
        numberOfRoomsMutableStateFlow.value = rooms + 1
    }

    fun onBathroomsValueChanged(bathrooms: Int) {
        if (bathrooms >= 0) {
            numberOfBathroomsMutableStateFlow.value = bathrooms
        }
    }

    fun decrementBathrooms(bathrooms: Int) {
        if (bathrooms > 0) {
            numberOfBathroomsMutableStateFlow.value = bathrooms - 1
        }
    }

    fun incrementBathrooms(bathrooms: Int) {
        numberOfBathroomsMutableStateFlow.value = bathrooms + 1
    }

    fun onBedroomsValueChanged(bedrooms: Int) {
        if (bedrooms >= 0) {
            numberOfBedroomsMutableStateFlow.value = bedrooms
        }
    }

    fun decrementBedrooms(bedrooms: Int) {
        if (bedrooms > 0) {
            numberOfBedroomsMutableStateFlow.value = bedrooms - 1
        }
    }

    fun incrementBedrooms(bedrooms: Int) {
        numberOfBedroomsMutableStateFlow.value = bedrooms + 1
    }

    fun onDateChanged(date: Any?) {
        val instant = Instant.ofEpochMilli(date as Long)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        dateMutableStateFlow.value = zonedDateTime.toLocalDate()
    }

    fun onAddressChanged(address: String) {
        currentAddressInputMutableStateFlow.value = address

        Log.d("test", "onAddressChanged: ${selectedAddressMutableStateFlow.value} $address")
        if (selectedAddressMutableStateFlow.value != null && address != selectedAddressMutableStateFlow.value!!.address) {
            selectedAddressMutableStateFlow.value = null
        }
    }
}
