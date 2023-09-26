package com.bakjoul.realestatemanager.ui.add

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
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
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
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

    private companion object {
        private const val TAG = "AddPropertyViewModel"
    }

    private val propertyFormMutableStateFlow: MutableStateFlow<AddPropertyForm> = MutableStateFlow(AddPropertyForm())

    private val currentAddressInputMutableStateFlow: MutableStateFlow<Pair<String, Boolean>?> = MutableStateFlow(null)
    private val addressPredictionsFlow: Flow<AutocompleteWrapper?> = currentAddressInputMutableStateFlow
        .transformLatest { addressData ->
            if (addressData == null) {
                emit(null)
            } else {
                val (input, fromUser) = addressData
                if (fromUser) {
                    if (input.isEmpty() || input.length < 5) {
                        emit(null)
                    } else {
                        delay(300.milliseconds)
                        emit(getAddressPredictionsUseCase.invoke(input))
                    }
                }
            }
        }

    private var isAddressTextCleared = false

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        combine(
            propertyFormMutableStateFlow,
            getCurrentCurrencyUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke(),
            addressPredictionsFlow,
            getPendingPhotosUseCase.invoke()
        ) { propertyForm, currency, surfaceUnit, addressPredictions, photos ->
            AddPropertyViewState(
                propertyTypeEntity = propertyForm.type,
                isSold = propertyForm.isSold,
                priceHint = formatPriceHint(currency),
                currencyFormat = getCurrencyFormat(currency),
                surfaceLabel = formatSurfaceLabel(surfaceUnit),
                surface = formatSurfaceValue(propertyForm.surface),
                numberOfRooms = (propertyForm.rooms ?: 0).toString(),
                numberOfBathrooms = propertyForm.bathrooms.toString(),
                numberOfBedrooms = propertyForm.bedrooms.toString(),
                addressPredictions = mapAddressPredictions(addressPredictions),
                address = formatAddress(propertyForm.autoCompleteAddress),
                city = propertyForm.address.city,
                state = propertyForm.address.state,
                zipcode = propertyForm.address.zipcode,
                photos = mapPhotosToItemViewStates(photos),
            )
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<AddPropertyViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.HideAddressSuggestions -> emit(Event(AddPropertyViewAction.HideSuggestions))
                is To.Camera -> emit(Event(AddPropertyViewAction.OpenCamera))
                is To.CloseAddProperty -> emit(Event(AddPropertyViewAction.CloseDialog))
                is To.Settings -> emit(Event(AddPropertyViewAction.OpenSettings))
                is To.Toast -> emit(Event(AddPropertyViewAction.ShowToast(it.message)))
                else -> Unit
            }
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

    private fun formatPriceHint(currency: AppCurrency): String = "Price (${currency.symbol})"

    private fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): String = "Surface (${surfaceUnit.unit})"

    private fun formatSurfaceValue(surface: BigDecimal): String = if (surface.scale() <= 0) {
        surface.toBigInteger().toString()
    } else {
        surface.toString()
    }

    private fun formatAddress(address: AddPropertyAddress?): String? {
        return if (address?.streetNumber != null && address.route != null) {
            "${address.streetNumber} ${address.route}"
        } else {
            null
        }
    }

    private fun mapAddressPredictions(wrapper: AutocompleteWrapper?): List<AddPropertySuggestionItemViewState> =
        (wrapper as? AutocompleteWrapper.Success)?.let {
            wrapper.predictions.map { predictionEntity ->
                AddPropertySuggestionItemViewState(
                    id = predictionEntity.placeId,
                    address = predictionEntity.address,
                    onSuggestionClicked = EquatableCallback {
                        navigateUseCase.invoke(To.HideAddressSuggestions)

                        viewModelScope.launch {
                            when (val geocodingResult = getAddressDetailsUseCase.invoke(predictionEntity.placeId)) {
                                is GeocodingWrapper.Error -> {
                                    navigateUseCase.invoke(To.Toast("An error occurred while trying to get selected address details"))
                                    Log.d(TAG, "Geocoding error: ${geocodingResult.exception.message}")
                                }

                                is GeocodingWrapper.Failure -> {
                                    navigateUseCase.invoke(To.Toast("Failed to get selected address details"))
                                    Log.d(TAG, "Geocoding failure: ${geocodingResult.message}")
                                }

                                is GeocodingWrapper.NoResults -> navigateUseCase.invoke(To.Toast("No results found for selected address"))

                                is GeocodingWrapper.Success -> {
                                    currentAddressInputMutableStateFlow.update {
                                        it?.copy(
                                            first = "${geocodingResult.result.streetNumber} ${geocodingResult.result.route}",
                                            second = false,
                                        )
                                    }
                                    propertyFormMutableStateFlow.update {
                                        it.copy(
                                            autoCompleteAddress = it.address.copy(
                                                streetNumber = geocodingResult.result.streetNumber,
                                                route = geocodingResult.result.route,
                                                complementaryAddress = it.address.complementaryAddress,
                                                zipcode = geocodingResult.result.zipcode,
                                                city = geocodingResult.result.city,
                                                state = geocodingResult.result.state,
                                                country = geocodingResult.result.country,
                                                latitude = geocodingResult.result.latitude,
                                                longitude = geocodingResult.result.longitude
                                            ),
                                            address = it.address.copy(
                                                streetNumber = geocodingResult.result.streetNumber,
                                                route = geocodingResult.result.route,
                                                complementaryAddress = it.address.complementaryAddress,
                                                zipcode = geocodingResult.result.zipcode,
                                                city = geocodingResult.result.city,
                                                state = geocodingResult.result.state,
                                                country = geocodingResult.result.country,
                                                latitude = geocodingResult.result.latitude,
                                                longitude = geocodingResult.result.longitude
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        } ?: emptyList()

    private fun resetAddressFields() {
        propertyFormMutableStateFlow.update {
            it.copy(
                autoCompleteAddress = null,
                address = AddPropertyAddress()
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

    fun updateRoomCount(rooms: Int) {
        propertyFormMutableStateFlow.update {
            it.copy(rooms = rooms)
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
            resetAddressFields()
            return
        }

        if (currentAddressInputMutableStateFlow.value?.first != address) {
            currentAddressInputMutableStateFlow.value = address to true
        }

        if (propertyFormMutableStateFlow.value.autoCompleteAddress != null
            && formatAddress(propertyFormMutableStateFlow.value.address) != address
        ) {
            resetAddressFields()
        }
    }

    fun onAddressTextCleared() {
        isAddressTextCleared = true
        currentAddressInputMutableStateFlow.value = "" to true
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

    // region Private data classes
    private data class AddPropertyForm(
        val type: PropertyTypeEntity? = null,
        val isSold: Boolean = false,
        val forSaleSince: LocalDate? = null,
        val dateOfSale: LocalDate? = null,
        val price: BigDecimal = BigDecimal.ZERO,
        val surface: BigDecimal = BigDecimal.ZERO,
        val rooms: Int? = null,
        val bathrooms: Int = 0,
        val bedrooms: Int = 0,
        val pointsOfInterest: List<PropertyPoiEntity> = emptyList(),
        val autoCompleteAddress: AddPropertyAddress? = null,
        val address: AddPropertyAddress = AddPropertyAddress(),
        val description: String? = null,
        val photos: List<PhotoEntity> = emptyList(),
        val agent: String? = null,
    )

    private data class AddPropertyAddress(
        val streetNumber: String? = null,
        val route: String? = null,
        val complementaryAddress: String? = null,
        val zipcode: String? = null,
        val city: String? = null,
        val state: String? = null,
        val country: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null
    )
    // endregion
}
