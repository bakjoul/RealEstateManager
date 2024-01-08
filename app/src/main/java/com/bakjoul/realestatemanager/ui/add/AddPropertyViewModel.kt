package com.bakjoul.realestatemanager.ui.add

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
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
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftByIdUseCase
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
import com.bakjoul.realestatemanager.ui.utils.NativeText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class AddPropertyViewModel @Inject constructor(
    getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getPropertyDraftByIdUseCase: GetPropertyDraftByIdUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
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
        private val SAVE_DELAY = 3.seconds
    }

    private val propertyFormMutableSharedFlow: MutableSharedFlow<PropertyFormEntity> = MutableSharedFlow(replay = 1)

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

    private val draftId: Long = requireNotNull(savedStateHandle.get<Long>("draftId")) {
        "No ID passed as parameter !"
    }

    private val isNewDraft = requireNotNull(savedStateHandle.get<Boolean>("isNewDraft")) {
        "No information about new draft passed as parameter !"
    }

    private var saveJob: Job? = null
    private var isAddressTextCleared = false

    private val propertyInformationFlow: Flow<PropertyInformation> = combine(
        propertyFormMutableSharedFlow,
        getCurrentCurrencyUseCase.invoke(),
        flow { emit(getEuroRateUseCase.invoke()) },
        getCurrentSurfaceUnitUseCase.invoke(),
    ) { propertyForm, currency, euroRate, surfaceUnit ->
        PropertyInformation(
            propertyForm,
            currency,
            euroRate,
            surfaceUnit
        )
    }.onEach { (propertyForm, currency, euroRate, surfaceUnit) ->
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(SAVE_DELAY)

            saveDraft(propertyForm, currency, euroRate.currencyRateEntity.rate, surfaceUnit)
        }
    }

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        if (latestValue == null) {
            if (isNewDraft) {
                propertyFormMutableSharedFlow.tryEmit(initPropertyForm(draftId))
                Log.d("test", "new draft case")
            } else {
                viewModelScope.launch {
                    val draft = getPropertyDraftByIdUseCase.invoke(draftId)
                    if (draft != null) {
                        propertyFormMutableSharedFlow.tryEmit(draft)
                        Log.d("test", "draft loaded : $draft")
                    }
                }
            }
        }

        combine(
            propertyInformationFlow,
            addressPredictionsFlow,
            getPhotosForPropertyIdUseCase.invoke(draftId),
            errorsMutableStateFlow
        ) { (propertyForm, currency, euroRate, surfaceUnit), addressPredictions, photos, errors ->
            AddPropertyViewState(
                propertyTypeEntity = propertyForm.type,
                forSaleSince = formatDate(propertyForm.forSaleSince),
                dateOfSale = formatDate(propertyForm.dateOfSale),
                isSold = propertyForm.isSold ?: false,
                price = formatSavedPrice(
                    propertyForm.referencePrice,
                    propertyForm.priceFromUser,
                    currency,
                    euroRate.currencyRateEntity.rate
                ),
                priceHint = formatPriceHint(currency),
                currencyFormat = getCurrencyFormat(currency),
                surfaceLabel = formatSurfaceLabel(surfaceUnit),
                surface = formatSurfaceValue(propertyForm.referenceSurface, propertyForm.surfaceFromUser, surfaceUnit),
                numberOfRooms = propertyForm.rooms ?: BigDecimal.ZERO,
                numberOfBathrooms = propertyForm.bathrooms ?: BigDecimal.ZERO,
                numberOfBedrooms = propertyForm.bedrooms ?: BigDecimal.ZERO,
                amenities = propertyForm.pointsOfInterest ?: emptyList(),
                addressPredictions = mapAddressPredictions(addressPredictions),
                address = formatAddress(propertyForm.autoCompleteAddress),
                complementaryAddress = propertyForm.address?.complementaryAddress,
                city = propertyForm.address?.city,
                state = propertyForm.address?.state,
                zipcode = propertyForm.address?.zipcode,
                description = propertyForm.description,
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

    private fun saveDraft(propertyForm: PropertyFormEntity, currency: AppCurrency, euroRate: Double, surfaceUnit: SurfaceUnit) {
        val priceInDollars = propertyForm.priceFromUser?.let {
            if (currency == AppCurrency.EUR) {
                it.fromEurosToDollars(euroRate)
            } else {
                it
            }
        } ?: propertyForm.referencePrice

        val surfaceInMeters = propertyForm.surfaceFromUser?.let {
            if (surfaceUnit == SurfaceUnit.FEET) {
                it.fromFeetSquaredToMeterSquared()
            } else {
                it
            }
        } ?: propertyForm.referenceSurface

        Log.d("test", "saveDraft: $propertyForm\npriceInDollars: $priceInDollars\nsurfaceInMeters: $surfaceInMeters")

        viewModelScope.launch {
            updatePropertyDraftUseCase.invoke(
                draftId,
                propertyForm.copy(referencePrice = priceInDollars, referenceSurface = surfaceInMeters)
            )
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
        referencePrice = null,
        priceFromUser = null,
        referenceSurface = null,
        surfaceFromUser = null,
        rooms = null,
        bathrooms = null,
        bedrooms = null,
        pointsOfInterest = emptyList(),
        autoCompleteAddress = null,
        address = PropertyFormAddress(),
        description = null,
        photos = emptyList(),
        agent = null,
        lastUpdate = LocalDateTime.now()
    )

    private fun formatDate(localDate: LocalDate?): NativeText? = localDate?.let {
        NativeText.Date(R.string.date_format, it)
    }

    private fun getCurrencyFormat(currency: AppCurrency): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = if (currency == AppCurrency.EUR) {
                ' '
            } else {
                ','
            }
            decimalSeparator = if (currency == AppCurrency.EUR) {
                ','
            } else {
                '.'
            }
        }

        return DecimalFormat("#,###.##", symbols)
    }

    private fun formatPriceHint(currency: AppCurrency): NativeText {
        return NativeText.Argument(
            R.string.add_property_price_hint,
            NativeText.Resource(currency.currencySymbol)
        )
    }

    private fun formatSavedPrice(
        referencePrice: BigDecimal?,
        priceFromUser: BigDecimal?,
        currency: AppCurrency,
        euroRate: Double
    ): String? {
        val convertedPrice = when (currency) {
            AppCurrency.EUR -> (priceFromUser ?: referencePrice)?.divide(BigDecimal(euroRate), 0, RoundingMode.CEILING)
            else -> priceFromUser ?: referencePrice
        }

        return convertedPrice?.toString()
    }

    private fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): NativeText {
        return NativeText.Argument(
            R.string.add_property_label_surface,
            NativeText.Resource(surfaceUnit.unitSymbol)
        )
    }

    private fun formatSurfaceValue(referenceSurface: BigDecimal?, surfaceFromUser: BigDecimal?, surfaceUnit: SurfaceUnit): BigDecimal =
        if (referenceSurface != null) {
            if (surfaceUnit == SurfaceUnit.FEET) {
                (surfaceFromUser ?: referenceSurface).fromMeterSquaredToFeetSquared()
            } else {
                referenceSurface
            }
        } else {
            BigDecimal.ZERO
        }

    private fun formatAddress(address: PropertyFormAddress?): String? = if (address?.streetNumber != null && address.route != null) {
        "${address.streetNumber} ${address.route}"
    } else {
        null
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

                                is GeocodingWrapper.Success -> {
                                    currentAddressInputMutableStateFlow.update {
                                        it?.copy(
                                            first = "${geocodingResult.result.streetNumber} ${geocodingResult.result.route}",
                                            second = false,
                                        )
                                    }

                                    val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
                                    val selectedAddress = PropertyFormAddress(
                                        streetNumber = geocodingResult.result.streetNumber,
                                        route = geocodingResult.result.route,
                                        complementaryAddress = propertyFormReplayCache.address?.complementaryAddress,
                                        zipcode = geocodingResult.result.zipcode,
                                        city = geocodingResult.result.city,
                                        state = geocodingResult.result.state,
                                        country = geocodingResult.result.country,
                                        latitude = geocodingResult.result.latitude,
                                        longitude = geocodingResult.result.longitude
                                    )
                                    propertyFormMutableSharedFlow.tryEmit(
                                        propertyFormReplayCache.copy(
                                            autoCompleteAddress = selectedAddress,
                                            address = selectedAddress
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }
        } ?: emptyList()

    private fun resetAddressFields() {
        Log.d("test", "resetAddressFields: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                autoCompleteAddress = null,
                address = PropertyFormAddress()
            )
        )
    }

    fun onPropertyTypeChanged(checkedId: Int) {
        Log.d("test", "onPropertyTypeChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
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
        )

        errorsMutableStateFlow.update {
            it.copy(isTypeErrorVisible = false)
        }
    }

    fun onSaleStatusChanged(isSold: Boolean) {
        Log.d("test", "onSaleStatusChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                isSold = isSold
            )
        )

        if (!isSold) {
            propertyFormMutableSharedFlow.tryEmit(
                propertyFormMutableSharedFlow.replayCache.first().copy(
                    dateOfSale = null
                )
            )
            errorsMutableStateFlow.update {
                it.copy(dateOfSaleError = null)
            }
        }
    }

    fun onForSaleSinceDateChanged(date: Long) {
        Log.d("test", "onForSaleSinceDateChanged: ")
        val instant = Instant.ofEpochMilli(date)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                forSaleSince = zonedDateTime.toLocalDate()
            )
        )

        errorsMutableStateFlow.update {
            it.copy(forSaleSinceError = null)
        }
    }

    fun onSoldOnDateChanged(date: Long) {
        Log.d("test", "onSoldOnDateChanged: ")
        val instant = Instant.ofEpochMilli(date)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                dateOfSale = zonedDateTime.toLocalDate()
            )
        )

        errorsMutableStateFlow.update {
            it.copy(dateOfSaleError = null)
        }
    }

    fun onPriceChanged(price: BigDecimal) {
        Log.d("test", "onPriceChanged: ")
        if (price >= BigDecimal.ZERO) {
            propertyFormMutableSharedFlow.tryEmit(
                propertyFormMutableSharedFlow.replayCache.first().copy(
                    priceFromUser = price
                )
            )
        }
    }

    fun onPriceTextCleared() {
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                priceFromUser = null
            )
        )

        errorsMutableStateFlow.update {
            it.copy(priceError = null)
        }
    }

    fun onSurfaceChanged(surface: BigDecimal) {
        Log.d("test", "onSurfaceChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                surfaceFromUser = surface
            )
        )
    }

    fun onRoomsCountChanged(rooms: BigDecimal) {
        Log.d("test", "onRoomsCountChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                rooms = rooms
            )
        )
    }

    fun onBathroomsCountChanged(bathrooms: BigDecimal) {
        Log.d("test", "onBathroomsCountChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                bathrooms = bathrooms
            )
        )
    }

    fun onBedroomsCountChanged(bedrooms: BigDecimal) {
        Log.d("test", "onBedroomsCountChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                bedrooms = bedrooms
            )
        )
    }

    fun onChipCheckedChanged(chipId: Int, isChecked: Boolean) {
        Log.d("test", "onChipCheckedChanged: $chipId")
        val poiEntity = PropertyPoiEntity.values().find { it.poiResId == chipId }

        if (poiEntity != null) {
            propertyFormMutableSharedFlow.tryEmit(
                propertyFormMutableSharedFlow.replayCache.first().copy(
                    pointsOfInterest = if (isChecked) {
                        propertyFormMutableSharedFlow.replayCache.first().pointsOfInterest?.plus(poiEntity)
                    } else {
                        propertyFormMutableSharedFlow.replayCache.first().pointsOfInterest?.minus(poiEntity)
                    }
                )
            )
        }
    }

    fun onAddressChanged(address: String) {
        Log.d("test", "onAddressChanged: ")
        errorsMutableStateFlow.update {
            it.copy(addressError = null)
        }

        // Reset address fields if address clear button was clicked
        if (isAddressTextCleared) {
            Log.d("test", "onAddressChanged: address text cleared")
            isAddressTextCleared = false
            resetAddressFields()
            return
        }

        // Updates current address if needed
        if (currentAddressInputMutableStateFlow.value?.first != address) {
            Log.d("test", "onAddressChanged: current address changed")
            currentAddressInputMutableStateFlow.value = address to true
        }

        // Reset address fields if current address input different from address selected from suggestions
        val propertyFormReplaceCache = propertyFormMutableSharedFlow.replayCache.first()
        if (propertyFormReplaceCache.autoCompleteAddress != null
            && formatAddress(propertyFormReplaceCache.address) != address
        ) {
            resetAddressFields()
        }
    }

    fun onAddressTextCleared() {
        isAddressTextCleared = true
        currentAddressInputMutableStateFlow.value = null

    }

    fun onComplementaryAddressChanged(complementaryAddress: String) {
        Log.d("test", "onComplementaryAddressChanged: ")
        val propertyFormReplaceCache = propertyFormMutableSharedFlow.replayCache.first()
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormReplaceCache.copy(
                address = propertyFormReplaceCache.address?.copy(complementaryAddress = complementaryAddress)
            )
        )
    }

    fun onComplementaryAddressTextCleared() {
        val propertyFormReplaceCache = propertyFormMutableSharedFlow.replayCache.first()
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormReplaceCache.copy(
                address = propertyFormReplaceCache.address?.copy(complementaryAddress = null)
            )
        )
    }

    fun onDescriptionChanged(description: String) {
        Log.d("test", "onDescriptionChanged: ")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(description = description)
        )

        if (description.isNotEmpty()) {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = null)
            }
        }
    }

    fun onDescriptionTextCleared() {
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(description = null)
        )
    }

    fun onCameraPermissionGranted() {
        navigateUseCase.invoke(To.Camera(draftId))
    }

    fun onChangeSettingsClicked() {
        navigateUseCase.invoke(To.Settings)
    }

    fun closeDialog() {
        val propertyFormReplaceCache = propertyFormMutableSharedFlow.replayCache.first()
        if (isNewDraft &&
            propertyFormReplaceCache.type == null &&
            propertyFormReplaceCache.forSaleSince == null &&
            propertyFormReplaceCache.dateOfSale == null &&
            propertyFormReplaceCache.priceFromUser == null &&
            propertyFormReplaceCache.referenceSurface == BigDecimal.ZERO &&
            propertyFormReplaceCache.rooms == BigDecimal.ZERO &&
            propertyFormReplaceCache.bathrooms == BigDecimal.ZERO &&
            propertyFormReplaceCache.bedrooms == BigDecimal.ZERO &&
            propertyFormReplaceCache.pointsOfInterest!!.isEmpty() &&
            propertyFormReplaceCache.address == PropertyFormAddress() &&
            propertyFormReplaceCache.autoCompleteAddress == null &&
            propertyFormReplaceCache.description == null
        ) {
            dropDraft()
        } else {
            navigateUseCase.invoke(To.SaveDraftDialog)
        }
    }

    fun dropDraft() {
        viewModelScope.launch {
            deletePropertyDraftUseCase.invoke(draftId)
            navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_draft_discarded)))
            navigateUseCase.invoke(To.CloseAddProperty)
        }
    }

    fun onSaveDraftButtonClicked() {
        if (saveJob?.isActive == true) {
            viewModelScope.launch {
                navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_saving_draft)))
                saveJob!!.join()
                navigateUseCase.invoke(To.CloseAddProperty)
            }
        } else {
            navigateUseCase.invoke(To.CloseAddProperty)
        }
    }

    fun onDoneButtonClicked() {
        if (isFormValid()) {
            viewModelScope.launch {
                val propertyFormReplaceCache = propertyFormMutableSharedFlow.replayCache.first()
                val newPropertyId = addPropertyUseCase.invoke(
                    PropertyEntity(
                        id = propertyFormReplaceCache.id,
                        type = propertyFormReplaceCache.type!!.name,
                        forSaleSince = propertyFormReplaceCache.forSaleSince!!,
                        saleDate = propertyFormReplaceCache.dateOfSale,
                        price = propertyFormReplaceCache.priceFromUser!!,
                        surface = propertyFormReplaceCache.surfaceFromUser!!,
                        rooms = propertyFormReplaceCache.rooms!!,
                        bathrooms = propertyFormReplaceCache.bathrooms!!,
                        bedrooms = propertyFormReplaceCache.bedrooms!!,
                        amenities = propertyFormReplaceCache.pointsOfInterest!!,
                        address = PropertyAddressEntity(
                            streetNumber = propertyFormReplaceCache.autoCompleteAddress!!.streetNumber!!,
                            route = propertyFormReplaceCache.autoCompleteAddress.route!!,
                            complementaryAddress = propertyFormReplaceCache.autoCompleteAddress.complementaryAddress,
                            zipcode = propertyFormReplaceCache.autoCompleteAddress.zipcode!!,
                            city = propertyFormReplaceCache.autoCompleteAddress.city!!,
                            state = propertyFormReplaceCache.autoCompleteAddress.state!!,
                            country = propertyFormReplaceCache.autoCompleteAddress.country!!,
                            latitude = propertyFormReplaceCache.autoCompleteAddress.latitude!!,
                            longitude = propertyFormReplaceCache.autoCompleteAddress.longitude!!
                        ),
                        description = propertyFormReplaceCache.description!!,
                        photos = propertyFormReplaceCache.photos!!,
                        agent = propertyFormReplaceCache.agent ?: "John Doe", // TODO: get agent name from settings
                        entryDate = ZonedDateTime.now().toLocalDate()
                    )
                )

                if (newPropertyId != null) {
                    deletePropertyDraftUseCase.invoke(newPropertyId)
                    navigateUseCase.invoke(To.CloseAddProperty)
                    Log.d("test", "onDoneButtonClicked: property added, draft deleted")
                } else {
                    Log.d("test", "onDoneButtonClicked: there was a problem adding the new property")
                }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val propertyFormReplaceCache = propertyFormMutableSharedFlow.replayCache.first()
        var isFormValid = true

        if (propertyFormReplaceCache.type == null) {
            errorsMutableStateFlow.update {
                it.copy(isTypeErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isTypeErrorVisible = false)
            }
        }

        if (propertyFormReplaceCache.forSaleSince == null) {
            errorsMutableStateFlow.update {
                it.copy(forSaleSinceError = NativeText.Resource(R.string.add_property_error_date_required))
            }
            isFormValid = false
        } else if (propertyFormReplaceCache.isSold == true &&
            propertyFormReplaceCache.dateOfSale != null &&
            propertyFormReplaceCache.forSaleSince.isAfter(propertyFormReplaceCache.dateOfSale)
        ) {
            errorsMutableStateFlow.update {
                it.copy(forSaleSinceError = NativeText.Resource(R.string.add_property_error_invalid_date))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(forSaleSinceError = null)
            }
        }

        if (propertyFormReplaceCache.isSold == true) {
            if (propertyFormReplaceCache.dateOfSale == null) {
                errorsMutableStateFlow.update {
                    it.copy(dateOfSaleError = NativeText.Resource(R.string.add_property_error_date_required))
                }
                isFormValid = false
            } else if (propertyFormReplaceCache.forSaleSince != null &&
                propertyFormReplaceCache.dateOfSale.isBefore(propertyFormReplaceCache.forSaleSince)
            ) {
                errorsMutableStateFlow.update {
                    it.copy(dateOfSaleError = NativeText.Resource(R.string.add_property_error_invalid_date))
                }
                isFormValid = false
            } else {
                errorsMutableStateFlow.update {
                    it.copy(dateOfSaleError = null)
                }
            }
        } else {
            errorsMutableStateFlow.update {
                it.copy(dateOfSaleError = null)
            }
        }

        if (propertyFormReplaceCache.priceFromUser == null) {
            errorsMutableStateFlow.update {
                it.copy(priceError = NativeText.Resource(R.string.add_property_error_price_required))
            }
            isFormValid = false
        } else if (propertyFormReplaceCache.priceFromUser <= BigDecimal.ZERO) {
            errorsMutableStateFlow.update {
                it.copy(priceError = NativeText.Resource(R.string.add_property_error_invalid_price))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(priceError = null)
            }
        }

        if (propertyFormReplaceCache.surfaceFromUser == null ||
            propertyFormReplaceCache.surfaceFromUser == BigDecimal.ZERO
        ) {
            errorsMutableStateFlow.update {
                it.copy(isSurfaceErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isSurfaceErrorVisible = false)
            }
        }

        if (propertyFormReplaceCache.rooms == null ||
            propertyFormReplaceCache.rooms < BigDecimal(1)
        ) {
            errorsMutableStateFlow.update {
                it.copy(isRoomsErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isRoomsErrorVisible = false)
            }
        }

        if (propertyFormReplaceCache.autoCompleteAddress == null) {
            errorsMutableStateFlow.update {
                it.copy(
                    addressError = NativeText.Resource(R.string.add_property_error_address_required),
                    cityError = NativeText.Simple(" "),
                    stateError = NativeText.Simple(" "),
                    zipcodeError = NativeText.Simple(" ")
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

        if (propertyFormReplaceCache.description.isNullOrEmpty()) {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = NativeText.Resource(R.string.add_property_error_description_required))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = null)
            }
        }

        return isFormValid
    }

    data class PropertyFormErrors(
        val isTypeErrorVisible: Boolean = false,
        val forSaleSinceError: NativeText? = null,
        val dateOfSaleError: NativeText? = null,
        val priceError: NativeText? = null,
        val isSurfaceErrorVisible: Boolean = false,
        val isRoomsErrorVisible: Boolean = false,
        val addressError: NativeText? = null,
        val cityError: NativeText? = null,
        val stateError: NativeText? = null,
        val zipcodeError: NativeText? = null,
        val descriptionError: NativeText? = null
    )

    private data class PropertyInformation(
        val propertyForm: PropertyFormEntity,
        val currency: AppCurrency,
        val euroRate: CurrencyRateWrapper,
        val surfaceUnit: SurfaceUnit,
    )

    private fun BigDecimal.fromFeetSquaredToMeterSquared(): BigDecimal = divide(BigDecimal(3.28084), 0, RoundingMode.CEILING)
    private fun BigDecimal.fromMeterSquaredToFeetSquared(): BigDecimal = times(BigDecimal(3.28084)).setScale(0, RoundingMode.CEILING)
    private fun BigDecimal.fromEurosToDollars(euroRate: Double): BigDecimal = times(BigDecimal(euroRate)).setScale(0, RoundingMode.CEILING)
}
