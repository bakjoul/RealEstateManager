package com.bakjoul.realestatemanager.ui.add

import android.app.Application
import android.widget.CompoundButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.property.PropertyPoi
import com.bakjoul.realestatemanager.data.property.PropertyType
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import com.bakjoul.realestatemanager.domain.geocoding.GetAddressDetailsUseCase
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.GetPhotosInMemoryUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase,
    private val getPhotosInMemoryUseCase: GetPhotosInMemoryUseCase,
    private val navigateUseCase: NavigateUseCase,
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase
) : ViewModel() {

    private val propertyTypeMutableStateFlow: MutableStateFlow<PropertyType?> = MutableStateFlow(null)
    private val dateMutableStateFlow: MutableStateFlow<LocalDate?> = MutableStateFlow(null)
    private val isForSaleMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val surfaceMutableStateFlow: MutableStateFlow<Double> = MutableStateFlow(0.0)
    private val numberOfRoomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numberOfBathroomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numberOfBedroomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val poiListMutableStateFlow: MutableStateFlow<Collection<PropertyPoi>> = MutableStateFlow(emptyList())
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
    private val selectedAddressDetailsFlow: Flow<GeocodingWrapper?> = selectedAddressMutableStateFlow
        .transformLatest { prediction ->
            if (prediction == null) {
                emit(null)
            } else {
                emit(getAddressDetailsUseCase.invoke(prediction.placeId))
            }
        }
    private val complementaryAddressInputMutableStateFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    private val descriptionInputMutableStateFlow: MutableStateFlow<String?> = MutableStateFlow(null)

    private var isAddressTextCleared = false
    private var isAddressTextUpdatedByAutocomplete = false
    private var currentAddress: String? = null
    private var state: String? = null
    private var city: String? = null
    private var zipcode: String? = null

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
            selectedAddressDetailsFlow,
            getPhotosInMemoryUseCase.invoke()
        ) { currency, surfaceUnit, propertyType, isForSale, surface, numberOfRooms, numberOfBathrooms, numberOfBedrooms, address, addressDetails, photos ->
            updateAddressData(addressDetails)
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
                address = currentAddress,
                state = state,
                city = city,
                zipcode = zipcode,
                photos = mapPhotosToItemViewStates(photos)
            )
        }.collect {
            emit(it)
        }
    }

    private fun mapPhotosToItemViewStates(photos: Map<String, String>): List<AddPropertyPhotoItemViewState> {
        return photos.entries.mapIndexed { index, entry ->
            AddPropertyPhotoItemViewState(
                id = index.toLong(),
                url = entry.key,
                description = entry.value,
                onPhotoClicked = EquatableCallback {
                    // TODO
                },
                onDeletePhotoClicked = EquatableCallback {
                    // TODO
                }
            )
        }
    }

    val viewActionLiveData: LiveData<Event<AddPropertyViewAction>> =
        getCurrentNavigationUseCase.invoke()
            .mapNotNull {
                when (it) {
                    is To.HideAddressSuggestions -> AddPropertyViewAction.HideSuggestions
                    is To.Camera -> AddPropertyViewAction.OpenCamera
                    is To.CloseAddProperty -> AddPropertyViewAction.CloseDialog
                    is To.Settings -> AddPropertyViewAction.OpenSettings
                    else -> null
                }
            }
            .map { Event(it) }
            .asLiveData()

    private fun formatDateHint(isForSale: Boolean): String {
        return if (isForSale) {
            application.getString(R.string.property_for_sale_since)
        } else {
            application.getString(R.string.property_sold_on)
        }
    }

    private fun formatPriceHint(currency: AppCurrency): String = "Price (${currency.symbol})"

    private fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): String = "Surface (${surfaceUnit.unit})"

    private fun formatSurfaceValue(surface: Double): String {
        return if (surface == surface.toInt().toDouble()) {
            surface.toInt().toString()
        } else {
            String.format("%.1f", surface)
        }
    }

    private fun mapAddressPredictions(wrapper: AutocompleteWrapper?): List<AddPropertySuggestionItemViewState> = when (wrapper) {
        is AutocompleteWrapper.Error,
        is AutocompleteWrapper.Failure -> emptyList()
        is AutocompleteWrapper.NoResults -> emptyList()
        is AutocompleteWrapper.Success -> wrapper.predictions.map { predictionEntity ->
            AddPropertySuggestionItemViewState(
                id = predictionEntity.placeId,
                address = predictionEntity.address,
                onSuggestionClicked = EquatableCallback {
                    navigateUseCase.invoke(To.HideAddressSuggestions)
                    selectedAddressMutableStateFlow.value = predictionEntity
                }
            )
        }

        null -> emptyList()
    }

    private fun updateAddressData(wrapper: GeocodingWrapper?) = when (wrapper) {
        is GeocodingWrapper.Error,
        is GeocodingWrapper.Failure -> null // TODO
        is GeocodingWrapper.NoResults -> null // TODO
        is GeocodingWrapper.Success -> {
            val result = wrapper.results.firstOrNull()
            if (result != null) {
                val streetNumberComponent = result.addressComponents.find { it.types.contains("street_number") }
                val routeComponent = result.addressComponents.find { it.types.contains("route") }
                val stateComponent = result.addressComponents.find { it.types.contains("administrative_area_level_1") }
                val cityComponent = result.addressComponents.find { it.types.contains("locality") }
                val zipcodeComponent = result.addressComponents.find { it.types.contains("postal_code") }

                if (streetNumberComponent != null
                    && routeComponent != null
                    && stateComponent != null
                    && cityComponent != null
                    && zipcodeComponent != null
                ) {
                    currentAddress = streetNumberComponent.longName + " " + routeComponent.longName
                    state = stateComponent.longName
                    city = cityComponent.longName
                    zipcode = zipcodeComponent.longName
                } else {
                    resetAddressFields()
                }
            } else {
                resetAddressFields()
            }
        }

        null -> resetAddressFields()
    }

    private fun resetAddressFields() {
        currentAddress = null
        state = null
        city = null
        zipcode = null
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

    fun onDateChanged(date: Any?) {
        val instant = Instant.ofEpochMilli(date as Long)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        dateMutableStateFlow.value = zonedDateTime.toLocalDate()
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

    fun onChipCheckedChanged(chip: CompoundButton, isChecked: Boolean) {
        val poi = when (chip.text) {
            PropertyPoi.School.name -> PropertyPoi.School
            PropertyPoi.Store.name -> PropertyPoi.Store
            PropertyPoi.Park.name -> PropertyPoi.Park
            PropertyPoi.Restaurant.name -> PropertyPoi.Restaurant
            PropertyPoi.Hospital.name -> PropertyPoi.Hospital
            PropertyPoi.Bus.name -> PropertyPoi.Bus
            PropertyPoi.Subway.name -> PropertyPoi.Subway
            PropertyPoi.Tramway.name -> PropertyPoi.Tramway
            PropertyPoi.Train.name -> PropertyPoi.Train
            PropertyPoi.Airport.name -> PropertyPoi.Airport
            else -> null
        }

        poi?.let {
            val currentList = poiListMutableStateFlow.value.toMutableList().apply {
                if (isChecked) add(it) else remove(it)
            }

            poiListMutableStateFlow.value = currentList
        }
    }

    fun onAddressChanged(address: String) {
        if (isAddressTextCleared) {
            isAddressTextCleared = false
            selectedAddressMutableStateFlow.value = null
            currentAddressInputMutableStateFlow.value = address
            complementaryAddressInputMutableStateFlow.value = null
            return
        }

        if (!isAddressTextUpdatedByAutocomplete) {
            val selectedAddress = selectedAddressMutableStateFlow.value
            currentAddressInputMutableStateFlow.value = address

            if (selectedAddress != null && address != currentAddress) {
                selectedAddressMutableStateFlow.value = null
                currentAddress = null
            }
        } else {
            isAddressTextUpdatedByAutocomplete = false
        }
    }

    fun onAddressTextUpdatedByAutocomplete() {
        isAddressTextUpdatedByAutocomplete = true
    }

    fun onAddressTextCleared() {
        isAddressTextCleared = true
    }

    fun onComplementaryAddressChanged(addressDetails: String) {
        complementaryAddressInputMutableStateFlow.value = addressDetails
    }

    fun onComplementaryAddressTextCleared() {
        complementaryAddressInputMutableStateFlow.value = null
    }

    fun onDescriptionChanged(description: String) {
        descriptionInputMutableStateFlow.value = description
    }

    fun onDescriptionTextCleared() {
        descriptionInputMutableStateFlow.value = null
    }

    fun onCameraPermissionGranted() {
        navigateUseCase.invoke(To.Camera)
    }

    fun onChangeSettingsClicked() {
        navigateUseCase.invoke(To.Settings)
    }

    fun closeDialog() {
        navigateUseCase.invoke(To.CloseAddProperty)
    }

    fun onDoneButtonClicked() {
        // TODO check everything
        navigateUseCase.invoke(To.CloseAddProperty)
    }
}
