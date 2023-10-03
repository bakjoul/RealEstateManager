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
import com.bakjoul.realestatemanager.domain.photos.DeletePhotoDraftUseCase
import com.bakjoul.realestatemanager.domain.photos.GetPhotosDraftsUseCase
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormAddress
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
    private val getPhotosDraftsUseCase: GetPhotosDraftsUseCase,
    private val deletePhotoDraftUseCase: DeletePhotoDraftUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "AddPropertyViewModel"
    }

    private val propertyFormMutableStateFlow: MutableStateFlow<PropertyFormEntity> = initPropertyForm()

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
            getPhotosDraftsUseCase.invoke()
        ) { propertyForm, currency, surfaceUnit, addressPredictions, photos ->
            AddPropertyViewState(
                propertyTypeEntity = propertyForm.type,
                isSold = propertyForm.isSold ?: false,
                priceHint = formatPriceHint(currency),
                currencyFormat = getCurrencyFormat(currency),
                surfaceLabel = formatSurfaceLabel(surfaceUnit),
                surface = formatSurfaceValue(propertyForm.surface),
                numberOfRooms = (propertyForm.rooms ?: 0).toString(),
                numberOfBathrooms = propertyForm.bathrooms.toString(),
                numberOfBedrooms = propertyForm.bedrooms.toString(),
                addressPredictions = mapAddressPredictions(addressPredictions),
                address = formatAddress(propertyForm.autoCompleteAddress),
                city = propertyForm.address?.city ?: "",
                state = propertyForm.address?.state ?: "",
                zipcode = propertyForm.address?.zipcode ?: "",
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

    private fun initPropertyForm() = MutableStateFlow(
        PropertyFormEntity(
            type = null,
            isSold = false,
            forSaleSince = null,
            dateOfSale = null,
            price = BigDecimal.ZERO,
            surface = BigDecimal.ZERO,
            rooms = 0,
            bathrooms = 0,
            bedrooms = 0,
            pointsOfInterest = emptyList(),
            autoCompleteAddress = null,
            address = PropertyFormAddress(),
            description = null,
            photos = emptyList(),
            agent = null,
        )
    )

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
                    deletePhotoDraftUseCase.invoke(it.id)
                }
            }
        )
    }

    private fun formatPriceHint(currency: AppCurrency): String = "Price (${currency.symbol})"

    private fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): String = "Surface (${surfaceUnit.unit})"

    private fun formatSurfaceValue(surface: BigDecimal?): String {
        return surface?.let {
            if (it.scale() <= 0) it.toBigInteger().toString() else it.toString()
        } ?: "0"
    }

    private fun formatAddress(address: PropertyFormAddress?): String? {
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
                                            autoCompleteAddress = it.address?.copy(
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
                                            address = it.address?.copy(
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
                address = PropertyFormAddress()
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

    fun onSurfaceChanged(surface: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(surface = BigDecimal(surface.toString()))
        }
    }

    fun onRoomsCountChanged(rooms: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(rooms = rooms.toInt())
        }
    }

    fun onBathroomsCountChanged(bathrooms: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(bathrooms = bathrooms.toInt())
        }
    }

    fun onBedroomsCountChanged(bedrooms: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(bedrooms = bedrooms.toInt())
        }
    }

    fun onChipCheckedChanged(chipText: String, isChecked: Boolean) {
        val poiEntity = PropertyPoiEntity.values().find { it.name.equals(chipText, ignoreCase = true) }

        if (poiEntity != null) {
            propertyFormMutableStateFlow.update {
                it.copy(
                    pointsOfInterest = if (isChecked) {
                        propertyFormMutableStateFlow.value.pointsOfInterest?.plus(poiEntity)
                    } else {
                        propertyFormMutableStateFlow.value.pointsOfInterest?.minus(poiEntity)
                    }
                )
            }
        }
    }

    fun onAddressChanged(address: String) {
        // Reset address fields if address clear button was clicked
        if (isAddressTextCleared) {
            isAddressTextCleared = false
            resetAddressFields()
            return
        }

        // Updates current address if needed
        if (currentAddressInputMutableStateFlow.value?.first != address) {
            currentAddressInputMutableStateFlow.value = address to true
        }

        // Reset address fields if current address input different from address selected from suggestions
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
            it.copy(address = it.address?.copy(complementaryAddress = complementaryAddress))
        }
    }

    fun onComplementaryAddressTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(address = it.address?.copy(complementaryAddress = null))
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
