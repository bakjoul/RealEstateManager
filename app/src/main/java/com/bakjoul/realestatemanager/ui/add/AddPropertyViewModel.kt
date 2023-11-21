package com.bakjoul.realestatemanager.ui.add

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListMapper
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.SelectType
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.geocoding.GetAddressDetailsUseCase
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.DeletePhotoUseCase
import com.bakjoul.realestatemanager.domain.photos.GetPhotosForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.DeletePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.UpdatePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormAddress
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
    savedStateHandle: SavedStateHandle,
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val getAddressPredictionsUseCase: GetAddressPredictionsUseCase,
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase,
    private val getPhotosForPropertyIdUseCase: GetPhotosForPropertyIdUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val deletePropertyDraftUseCase: DeletePropertyDraftUseCase,
    private val updatePropertyDraftUseCase: UpdatePropertyDraftUseCase,
    private val addPropertyUseCase: AddPropertyUseCase,
) : ViewModel() {

    private companion object {
        private const val TAG = "AddPropertyViewModel"
        private const val SAVE_DELAY = 3000L
    }

    private val propertyFormMutableStateFlow: MutableStateFlow<PropertyFormEntity> = MutableStateFlow(PropertyFormEntity())

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
    private val errorsMutableStateFlow: MutableStateFlow<PropertyFormErrors> = MutableStateFlow(PropertyFormErrors())

    private var propertyId: Long? = null
    private var isNewDraft = false
    private var hasPropertyFormChanged = false
    private var lastSavedPropertyForm: PropertyFormEntity? = null
    private var saveJob: Job? = null
    private var isAddressTextCleared = false

    init {
        if (savedStateHandle.get<Long>("propertyId") != null) {
            propertyId = savedStateHandle.get<Long>("propertyId")
        } else {
            propertyId = savedStateHandle.get<Long>("propertyDraftId")
            propertyFormMutableStateFlow.value = initPropertyForm(propertyId!!)
            isNewDraft = true
        }
        lastSavedPropertyForm = propertyFormMutableStateFlow.value.copy()
    }

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        combine(
            propertyFormMutableStateFlow,
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke()) },
            getCurrentSurfaceUnitUseCase.invoke(),
            addressPredictionsFlow,
            getPhotosForPropertyIdUseCase.invoke(propertyId),
            errorsMutableStateFlow
        ) { propertyForm, currency, euroRate, surfaceUnit, addressPredictions, photos, errors ->
            if (hasPropertyFormChanged) {
                saveJob?.cancelAndJoin()
            }

            if (propertyForm != lastSavedPropertyForm) {
                saveJob = viewModelScope.launch {
                    delay(SAVE_DELAY)

                    saveDraft(propertyForm, currency, euroRate.currencyRateEntity.rate)
                    lastSavedPropertyForm = propertyForm
                    hasPropertyFormChanged = false
                }
            }

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
                photos = PhotoListMapper().map(
                    photos,
                    { SelectType.NOT_SELECTABLE },
                    {},
                    {
                        viewModelScope.launch {
                            deletePhotoUseCase.invoke(it)
                        }
                    }
                ),
                isTypeErrorVisible = errors.isTypeErrorVisible,
                forSaleSinceError = errors.forSaleSinceError,
                dateOfSaleError = errors.dateOfSaleError,
                priceError = errors.priceError,
                isSurfaceErrorVisible = errors.isSurfaceErrorVisible,
                isRoomsErrorVisible = errors.isRoomsErrorVisible,
                addressError = errors.addressError,
                cityError = errors.cityError,
                stateError = errors.stateError,
                zipcodeError = errors.zipcodeError,
                descriptionError = errors.descriptionError,
            )
        }.collect {
            emit(it)
        }
    }

    private fun saveDraft(propertyForm: PropertyFormEntity, currency: AppCurrency, euroRate: Double) {
        val price = if (currency == AppCurrency.EUR) {
            propertyForm.price?.times(BigDecimal(euroRate))
        } else {
            propertyForm.price
        }

        viewModelScope.launch {
            updatePropertyDraftUseCase.invoke(propertyId!!, propertyForm.copy(price = price))
        }
    }

    val viewActionLiveData: LiveData<Event<AddPropertyViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.HideAddressSuggestions -> emit(Event(AddPropertyViewAction.HideSuggestions))
                is To.Camera -> emit(Event(AddPropertyViewAction.OpenCamera(it.propertyId)))
                is To.SaveDraftDialog -> emit(Event(AddPropertyViewAction.SaveDraftDialog))
                is To.CloseAddProperty -> emit(Event(AddPropertyViewAction.CloseDialog))
                is To.Settings -> emit(Event(AddPropertyViewAction.OpenSettings))
                is To.Toast -> emit(Event(AddPropertyViewAction.ShowToast(it.message)))
                else -> Unit
            }
        }
    }

    private fun initPropertyForm(propertyDraftId: Long) = PropertyFormEntity(
        id = propertyDraftId,
        type = null,
        isSold = false,
        forSaleSince = null,
        dateOfSale = null,
        price = null,
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
        entryDate = null
    )

    private fun getCurrencyFormat(currency: AppCurrency): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = if (currency == AppCurrency.EUR) ' ' else ','
        symbols.decimalSeparator = if (currency == AppCurrency.EUR) ',' else '.'

        return DecimalFormat("#,###.##", symbols)
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

        errorsMutableStateFlow.update {
            it.copy(isTypeErrorVisible = false)
        }

        hasPropertyFormChanged = true
    }

    fun onSaleStatusChanged(isSold: Boolean) {
        propertyFormMutableStateFlow.update {
            it.copy(isSold = isSold)
        }

        if (!isSold) {
            propertyFormMutableStateFlow.update {
                it.copy(dateOfSale = null)
            }
            errorsMutableStateFlow.update {
                it.copy(dateOfSaleError = null)
            }
        }

        hasPropertyFormChanged = true
    }

    fun onForSaleSinceDateChanged(date: Any?) {
        val instant = Instant.ofEpochMilli(date as Long)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        propertyFormMutableStateFlow.update {
            it.copy(forSaleSince = zonedDateTime.toLocalDate())
        }

        errorsMutableStateFlow.update {
            it.copy(forSaleSinceError = null)
        }

        hasPropertyFormChanged = true
    }

    fun onSoldOnDateChanged(date: Any?) {
        val instant = Instant.ofEpochMilli(date as Long)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        propertyFormMutableStateFlow.update {
            it.copy(dateOfSale = zonedDateTime.toLocalDate())
        }

        errorsMutableStateFlow.update {
            it.copy(dateOfSaleError = null)
        }

        hasPropertyFormChanged = true
    }

    fun onPriceChanged(price: BigDecimal) {
        if (price >= BigDecimal.ZERO) {
            propertyFormMutableStateFlow.update {
                it.copy(price = price)
            }

            hasPropertyFormChanged = true
        }
    }

    fun onPriceTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(price = BigDecimal.ZERO)
        }

        errorsMutableStateFlow.update {
            it.copy(priceError = null)
        }

        hasPropertyFormChanged = true
    }

    fun onSurfaceChanged(surface: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(surface = BigDecimal(surface.toString()))
        }

        hasPropertyFormChanged = true
    }

    fun onRoomsCountChanged(rooms: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(rooms = rooms.toInt())
        }

        hasPropertyFormChanged = true
    }

    fun onBathroomsCountChanged(bathrooms: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(bathrooms = bathrooms.toInt())
        }

        hasPropertyFormChanged = true
    }

    fun onBedroomsCountChanged(bedrooms: Number) {
        propertyFormMutableStateFlow.update {
            it.copy(bedrooms = bedrooms.toInt())
        }

        hasPropertyFormChanged = true
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

            hasPropertyFormChanged = true
        }
    }

    fun onAddressChanged(address: String) {
        errorsMutableStateFlow.update {
            it.copy(addressError = null)
        }
        hasPropertyFormChanged = true

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

        hasPropertyFormChanged = true
    }

    fun onComplementaryAddressChanged(complementaryAddress: String) {
        propertyFormMutableStateFlow.update {
            it.copy(address = it.address?.copy(complementaryAddress = complementaryAddress))
        }

        hasPropertyFormChanged = true
    }

    fun onComplementaryAddressTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(address = it.address?.copy(complementaryAddress = null))
        }

        hasPropertyFormChanged = true
    }

    fun onDescriptionChanged(description: String) {
        propertyFormMutableStateFlow.update {
            it.copy(description = description)
        }

        if (description.isNotEmpty()) {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = null)
            }
        }

        hasPropertyFormChanged = true
    }

    fun onDescriptionTextCleared() {
        propertyFormMutableStateFlow.update {
            it.copy(description = null)
        }

        hasPropertyFormChanged = true
    }

    fun onCameraPermissionGranted() {
        navigateUseCase.invoke(To.Camera(propertyId!!))
    }

    fun onChangeSettingsClicked() {
        navigateUseCase.invoke(To.Settings)
    }

    fun closeDialog() {
        if (isNewDraft &&
            propertyFormMutableStateFlow.value.type == null &&
            propertyFormMutableStateFlow.value.forSaleSince == null &&
            propertyFormMutableStateFlow.value.dateOfSale == null &&
            propertyFormMutableStateFlow.value.price == null &&
            propertyFormMutableStateFlow.value.surface == BigDecimal.ZERO &&
            propertyFormMutableStateFlow.value.rooms == 0 &&
            propertyFormMutableStateFlow.value.bathrooms == 0 &&
            propertyFormMutableStateFlow.value.bedrooms == 0 &&
            propertyFormMutableStateFlow.value.pointsOfInterest!!.isEmpty() &&
            propertyFormMutableStateFlow.value.address == PropertyFormAddress() &&
            propertyFormMutableStateFlow.value.autoCompleteAddress == null &&
            propertyFormMutableStateFlow.value.description == null) {

            dropDraft()
        } else {
            navigateUseCase.invoke(To.SaveDraftDialog)
        }
    }

    fun dropDraft() {
        viewModelScope.launch {
            deletePropertyDraftUseCase.invoke(propertyId!!)
            navigateUseCase.invoke(To.Toast("Draft has been discarded"))
            navigateUseCase.invoke(To.CloseAddProperty)
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            updatePropertyDraftUseCase.invoke(propertyId!!, propertyFormMutableStateFlow.value)
            navigateUseCase.invoke(To.Toast("Draft has been saved"))
            navigateUseCase.invoke(To.CloseAddProperty)
        }
    }

    fun onDoneButtonClicked() {
        if (isFormValid()) {
            viewModelScope.launch {
                val newPropertyId = async {
                    addPropertyUseCase.invoke(
                        PropertyEntity(
                            id = propertyFormMutableStateFlow.value.id,
                            type = propertyFormMutableStateFlow.value.type!!.name,
                            forSaleSince = propertyFormMutableStateFlow.value.forSaleSince!!,
                            saleDate = propertyFormMutableStateFlow.value.dateOfSale,
                            price = propertyFormMutableStateFlow.value.price!!,
                            surface = propertyFormMutableStateFlow.value.surface!!,
                            rooms = propertyFormMutableStateFlow.value.rooms!!,
                            bathrooms = propertyFormMutableStateFlow.value.bathrooms!!,
                            bedrooms = propertyFormMutableStateFlow.value.bedrooms!!,
                            amenities = propertyFormMutableStateFlow.value.pointsOfInterest!!,
                            address = PropertyAddressEntity(
                                streetNumber = propertyFormMutableStateFlow.value.autoCompleteAddress!!.streetNumber!!,
                                route = propertyFormMutableStateFlow.value.autoCompleteAddress!!.route!!,
                                complementaryAddress = propertyFormMutableStateFlow.value.autoCompleteAddress!!.complementaryAddress,
                                zipcode = propertyFormMutableStateFlow.value.autoCompleteAddress!!.zipcode!!,
                                city = propertyFormMutableStateFlow.value.autoCompleteAddress!!.city!!,
                                state = propertyFormMutableStateFlow.value.autoCompleteAddress!!.state!!,
                                country = propertyFormMutableStateFlow.value.autoCompleteAddress!!.country!!,
                                latitude = propertyFormMutableStateFlow.value.autoCompleteAddress!!.latitude!!,
                                longitude = propertyFormMutableStateFlow.value.autoCompleteAddress!!.longitude!!
                            ),
                            description = propertyFormMutableStateFlow.value.description!!,
                            photos = propertyFormMutableStateFlow.value.photos!!,
                            agent = propertyFormMutableStateFlow.value.agent ?: "John Doe",
                            entryDate = ZonedDateTime.now().toLocalDate()
                        )
                    )
                }.await()

                if (newPropertyId != null) {
                    deletePropertyDraftUseCase.invoke(newPropertyId)
                    navigateUseCase.invoke(To.CloseAddProperty)
                    Log.d("test", "onDoneButtonClicked: property added, draft deleted")
                } else {
                    Log.d("test", "onDoneButtonClicked: there was a problem adding the new property")
                }
            }
        } else {
            Log.d("test", "onDoneButtonClicked: else")
        }
    }

    private fun isFormValid() : Boolean {
        var isFormValid = true

        if (propertyFormMutableStateFlow.value.type == null) {
            errorsMutableStateFlow.update {
                it.copy(isTypeErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isTypeErrorVisible = false)
            }
        }

        if (propertyFormMutableStateFlow.value.forSaleSince == null) {
            errorsMutableStateFlow.update {
                it.copy(forSaleSinceError = "Date required")
            }
            isFormValid = false
        } else {
            if (propertyFormMutableStateFlow.value.isSold == true && propertyFormMutableStateFlow.value.dateOfSale != null && propertyFormMutableStateFlow.value.forSaleSince!!.isAfter(propertyFormMutableStateFlow.value.dateOfSale)) {
                errorsMutableStateFlow.update {
                    it.copy(forSaleSinceError = "Invalid date")
                }
                isFormValid = false
            } else {
                errorsMutableStateFlow.update {
                    it.copy(forSaleSinceError = null)
                }
            }
        }

        if (propertyFormMutableStateFlow.value.isSold == true) {
            if (propertyFormMutableStateFlow.value.dateOfSale == null) {
                errorsMutableStateFlow.update {
                    it.copy(dateOfSaleError = "Date required")
                }
                isFormValid = false
            } else {
                if (propertyFormMutableStateFlow.value.forSaleSince != null && propertyFormMutableStateFlow.value.dateOfSale!!.isBefore(propertyFormMutableStateFlow.value.forSaleSince)) {
                    errorsMutableStateFlow.update {
                        it.copy(dateOfSaleError = "Invalid date")
                    }
                    isFormValid = false
                } else {
                    errorsMutableStateFlow.update {
                        it.copy(dateOfSaleError = null)
                    }
                }
            }
        } else {
            errorsMutableStateFlow.update {
                it.copy(dateOfSaleError = null)
            }
        }

        if (propertyFormMutableStateFlow.value.price == null) {
            errorsMutableStateFlow.update {
                it.copy(priceError = "Price required")
            }
            isFormValid = false
        } else {
            if (propertyFormMutableStateFlow.value.price!! <= BigDecimal.ZERO) {
                errorsMutableStateFlow.update {
                    it.copy(priceError = "Invalid price")
                }
                isFormValid = false
            } else {
                errorsMutableStateFlow.update {
                    it.copy(priceError = null)
                }
            }
        }

        if (propertyFormMutableStateFlow.value.surface!! <= BigDecimal.ZERO) {
            errorsMutableStateFlow.update {
                it.copy(isSurfaceErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isSurfaceErrorVisible = false)
            }
        }

        if (propertyFormMutableStateFlow.value.rooms!! <= 0) {
            errorsMutableStateFlow.update {
                it.copy(isRoomsErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isRoomsErrorVisible = false)
            }
        }

        if (propertyFormMutableStateFlow.value.autoCompleteAddress == null) {
            errorsMutableStateFlow.update {
                it.copy(
                    addressError = "Address required",
                    cityError = " ",
                    stateError = " ",
                    zipcodeError = " "
                )
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(
                    addressError = null,
                    cityError = null,
                    stateError = null,
                    zipcodeError = null
                )
            }
        }

        if (propertyFormMutableStateFlow.value.description == null ||
            propertyFormMutableStateFlow.value.description!!.isEmpty()) {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = "Description required")
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = null)
            }
        }

        return isFormValid
    }

    data class PropertyFormErrors (
        val isTypeErrorVisible: Boolean = false,
        val forSaleSinceError: String? = null,
        val dateOfSaleError: String? = null,
        val priceError: String? = null,
        val isSurfaceErrorVisible: Boolean = false,
        val isRoomsErrorVisible: Boolean = false,
        val addressError: String? = null,
        val cityError: String? = null,
        val stateError: String? = null,
        val zipcodeError: String? = null,
        val descriptionError: String? = null
    )
}
