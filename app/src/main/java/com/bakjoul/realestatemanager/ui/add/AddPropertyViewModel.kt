package com.bakjoul.realestatemanager.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.add.model.AddPropertyFormEntity
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import com.bakjoul.realestatemanager.domain.geocoding.GetAddressDetailsUseCase
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.DeletePendingPhotoUseCase
import com.bakjoul.realestatemanager.domain.photos.GetPendingPhotosUseCase
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.EquatableCallbackWithTwoParams
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AddPropertyViewModel @Inject constructor(
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val getAddressPredictionsUseCase: GetAddressPredictionsUseCase,
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase,
    private val getPendingPhotosUseCase: GetPendingPhotosUseCase,
    private val deletePendingPhotoUseCase: DeletePendingPhotoUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val propertyFormMutableStateFlow: MutableStateFlow<AddPropertyFormEntity> = MutableStateFlow(AddPropertyFormEntity())

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

    private var isAddressTextCleared = false
    private var isAddressTextUpdatedByAutocomplete = false
    private var currentAddress: String? = null

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        combine(
            propertyFormMutableStateFlow,
            getCurrentCurrencyUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke(),
            addressPredictionsFlow,
            selectedAddressDetailsFlow,
            getPendingPhotosUseCase.invoke()
        ) { propertyForm, currency, surfaceUnit, addressPredictions, addressDetails, photos ->
            updateAddressData(addressDetails)

            AddPropertyViewState(
                propertyTypeEntity = propertyForm.type,
                isSold = propertyForm.isSold,
                priceHint = formatPriceHint(currency),
                currencyFormat = getCurrencyFormat(currency),
                surfaceLabel = formatSurfaceLabel(surfaceUnit),
                surface = formatSurfaceValue(propertyForm.surface),
                numberOfRooms = propertyForm.rooms.toString(),
                numberOfBathrooms = propertyForm.bathrooms.toString(),
                numberOfBedrooms = propertyForm.bedrooms.toString(),
                addressPredictions = mapAddressPredictions(addressPredictions),
                address = currentAddress,
                state = propertyForm.address.state,
                city = propertyForm.address.city,
                zipcode = propertyForm.address.zipcode,
                photos = mapPhotosToItemViewStates(photos),
            )
        }.collect {
            emit(it)
        }
    }

    private fun getCurrencyFormat(currency: AppCurrency): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = if (currency == AppCurrency.EUR) ' ' else ','
        symbols.decimalSeparator = if (currency == AppCurrency.EUR) ',' else '.'

        return DecimalFormat("#,###.##", symbols)
    }

    private fun mapPhotosToItemViewStates(photos: List<PhotoEntity>): List<AddPropertyPhotoItemViewState> = photos.map {
        AddPropertyPhotoItemViewState(
            id = it.id,
            url = it.url,
            description = it.description,
            onPhotoClicked = EquatableCallback {
                // TODO
            },
            onDeletePhotoClicked = EquatableCallback {
                viewModelScope.launch {
                    deletePendingPhotoUseCase.invoke(it.id)
                }
            }
        )
    }

    val viewActionLiveData: LiveData<Event<AddPropertyViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.HideAddressSuggestions -> emit(Event(AddPropertyViewAction.HideSuggestions))
                is To.Camera -> emit(Event(AddPropertyViewAction.OpenCamera))
                is To.CloseAddProperty -> emit(Event(AddPropertyViewAction.CloseDialog))
                is To.Settings -> emit(Event(AddPropertyViewAction.OpenSettings))
                else -> Unit
            }
        }
    }

    private fun formatPriceHint(currency: AppCurrency): String = "Price (${currency.symbol})"

    private fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): String = "Surface (${surfaceUnit.unit})"

    private fun formatSurfaceValue(surface: BigDecimal): String = if (surface.scale() <= 0) {
        surface.toBigInteger().toString()
    } else {
        surface.toString()
    }

    private fun mapAddressPredictions(wrapper: AutocompleteWrapper?): List<AddPropertySuggestionItemViewState> =
        (wrapper as? AutocompleteWrapper.Success)?.let {
            wrapper.predictions.map { predictionEntity ->
                AddPropertySuggestionItemViewState(
                    id = predictionEntity.placeId,
                    address = predictionEntity.address,
                    onSuggestionClicked = EquatableCallback {
                        navigateUseCase.invoke(To.HideAddressSuggestions)
                        selectedAddressMutableStateFlow.value = predictionEntity
                    }
                )
            }
        } ?: emptyList()

    private fun updateAddressData(wrapper: GeocodingWrapper?) {
        when (wrapper) {
            is GeocodingWrapper.Error,
            is GeocodingWrapper.Failure,
            is GeocodingWrapper.NoResults -> return
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
                        currentAddress = "${streetNumberComponent.longName} ${routeComponent.longName}"
                        propertyFormMutableStateFlow.update {
                            it.copy(
                                address = it.address.copy(
                                    address = streetNumberComponent.longName + " " + routeComponent.longName,
                                    complementaryAddress = it.address.complementaryAddress,
                                    state = stateComponent.longName,
                                    city = cityComponent.longName,
                                    zipcode = zipcodeComponent.longName
                                )
                            )
                        }
                    } else {
                        resetAddressFields()
                    }
                } else {
                    resetAddressFields()
                }
            }

            null -> resetAddressFields()
        }
    }

    private fun resetAddressFields() {
        currentAddress = null
        propertyFormMutableStateFlow.update {
            it.copy(
                address = it.address.copy(
                    address = null,
                    complementaryAddress = null,
                    state = null,
                    city = null,
                    zipcode = null
                )
            )
        }
    }

    fun onPropertyTypeChanged(checkedId: Int) {
        propertyFormMutableStateFlow.update {
            it.copy(
                type = when (checkedId) {
                    R.id.add_property_type_flat_RadioButton -> PropertyTypeEntity.FLAT
                    R.id.add_property_type_house_RadioButton -> PropertyTypeEntity.HOUSE
                    R.id.add_property_type_duplex_RadioButton -> PropertyTypeEntity.DUPLEX
                    R.id.add_property_type_penthouse_RadioButton -> PropertyTypeEntity.PENTHOUSE
                    R.id.add_property_type_loft_RadioButton -> PropertyTypeEntity.LOFT
                    R.id.add_property_type_other_RadioButton -> PropertyTypeEntity.OTHER
                    else -> null
                }
            )
        }
    }

    fun onSaleStatusChanged(isSold: Boolean) {
        propertyFormMutableStateFlow.update {
            it.copy(isSold = isSold)
        }
    }

    fun onForSaleSinceDateChanged(date: Any?) {
        val instant = Instant.ofEpochMilli(date as Long)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        propertyFormMutableStateFlow.update {
            it.copy(forSaleSince = zonedDateTime.toLocalDate())
        }
    }

    fun onSoldOnDateChanged(date: Any?) {
        val instant = Instant.ofEpochMilli(date as Long)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        propertyFormMutableStateFlow.update {
            it.copy(dateOfSale = zonedDateTime.toLocalDate())
        }
    }

    fun onPriceChanged(price: BigDecimal) {
        if (price >= BigDecimal.ZERO) {
            propertyFormMutableStateFlow.update {
                it.copy(price = price)
            }
        }
    }

    fun onPriceTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(price = BigDecimal.ZERO)
        }
    }

    fun onSurfaceValueChanged(surface: BigDecimal) {
        if (surface >= BigDecimal.ZERO) {
            propertyFormMutableStateFlow.update {
                it.copy(surface = surface)
            }
        }
    }

    fun decrementSurface(surface: BigDecimal) {
        if (surface > BigDecimal.ZERO) {
            propertyFormMutableStateFlow.update {
                it.copy(surface = surface - BigDecimal.ONE)
            }
        }
    }

    fun incrementSurface(surface: BigDecimal) {
        propertyFormMutableStateFlow.update {
            it.copy(surface = surface + BigDecimal.ONE)
        }
    }

    fun onRoomsValueChanged(rooms: Int) {
        if (rooms >= 0) {
            propertyFormMutableStateFlow.update {
                it.copy(rooms = rooms)
            }
        }
    }

    fun decrementRooms(rooms: Int) {
        if (rooms > 0) {
            propertyFormMutableStateFlow.update {
                it.copy(rooms = rooms - 1)
            }
        }
    }

    fun incrementRooms(rooms: Int) {
        propertyFormMutableStateFlow.update {
            it.copy(rooms = rooms + 1)
        }
    }

    fun onBathroomsValueChanged(bathrooms: Int) {
        if (bathrooms >= 0) {
            propertyFormMutableStateFlow.update {
                it.copy(bathrooms = bathrooms)
            }
        }
    }

    fun decrementBathrooms(bathrooms: Int) {
        if (bathrooms > 0) {
            propertyFormMutableStateFlow.update {
                it.copy(bathrooms = bathrooms - 1)
            }
        }
    }

    fun incrementBathrooms(bathrooms: Int) {
        propertyFormMutableStateFlow.update {
            it.copy(bathrooms = bathrooms + 1)
        }
    }

    fun onBedroomsValueChanged(bedrooms: Int) {
        if (bedrooms >= 0) {
            propertyFormMutableStateFlow.update {
                it.copy(bedrooms = bedrooms)
            }
        }
    }

    fun decrementBedrooms(bedrooms: Int) {
        if (bedrooms > 0) {
            propertyFormMutableStateFlow.update {
                it.copy(bedrooms = bedrooms - 1)
            }
        }
    }

    fun incrementBedrooms(bedrooms: Int) {
        propertyFormMutableStateFlow.update {
            it.copy(bedrooms = bedrooms + 1)
        }
    }

    fun onChipCheckedChanged(chipText: String, isChecked: Boolean) {
        val poiEntity = PropertyPoiEntity.values().find { it.name.equals(chipText, ignoreCase = true) }

        if (poiEntity != null) {
            propertyFormMutableStateFlow.update {
                it.copy(
                    pointsOfInterest = if (isChecked) {
                        propertyFormMutableStateFlow.value.pointsOfInterest + poiEntity
                    } else {
                        propertyFormMutableStateFlow.value.pointsOfInterest - poiEntity
                    }
                )
            }
        }
    }

    fun onAddressChanged(address: String) {
        if (isAddressTextCleared) {
            isAddressTextCleared = false

            selectedAddressMutableStateFlow.value = null
            currentAddressInputMutableStateFlow.value = address
            propertyFormMutableStateFlow.update {
                it.copy(address = it.address.copy(complementaryAddress = null))
            }
            return
        }

        if (!isAddressTextUpdatedByAutocomplete) {
            val selectedAddress = selectedAddressMutableStateFlow.value
            currentAddressInputMutableStateFlow.value = address

            if (selectedAddress != null && address != currentAddress) {
                selectedAddressMutableStateFlow.value = null
                resetAddressFields()
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

    fun onComplementaryAddressChanged(complementaryAddress: String) {
        propertyFormMutableStateFlow.update {
            it.copy(address = it.address.copy(complementaryAddress = complementaryAddress))
        }
    }

    fun onComplementaryAddressTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(address = it.address.copy(complementaryAddress = null))
        }
    }

    fun onDescriptionChanged(description: String) {
        propertyFormMutableStateFlow.update {
            it.copy(description = description)
        }
    }

    fun onDescriptionTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(description = null)
        }
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
