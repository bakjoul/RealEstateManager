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
import com.bakjoul.realestatemanager.domain.photo_preview.SetLastPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.photos.AddPhotosUseCase
import com.bakjoul.realestatemanager.domain.photos.DeletePhotosUseCase
import com.bakjoul.realestatemanager.domain.photos.GetPhotosForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.photos.UpdatePhotosForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.photos.content_resolver.SavePhotosToAppFilesUseCase
import com.bakjoul.realestatemanager.domain.photos.edit.AddPhotoToExistingPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.photos.edit.DeletePhotosForExistingPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.photos.edit.GetPhotosForExistingPropertyDraftIdUseCase
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.UpdatePropertyUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.DeletePropertyDraftWithPhotosUseCase
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
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.getCurrencyFormat
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
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class AddPropertyViewModel @Inject constructor(
    getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    savedStateHandle: SavedStateHandle,
    getPhotosForPropertyIdUseCase: GetPhotosForPropertyIdUseCase,
    getPhotosForExistingPropertyDraftIdUseCase: GetPhotosForExistingPropertyDraftIdUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getPropertyDraftByIdUseCase: GetPropertyDraftByIdUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val getAddressPredictionsUseCase: GetAddressPredictionsUseCase,
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase,
    private val deletePhotosUseCase: DeletePhotosUseCase,
    private val deletePhotosForExistingPropertyDraftUseCase: DeletePhotosForExistingPropertyDraftUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val deletePropertyDraftWithPhotosUseCase: DeletePropertyDraftWithPhotosUseCase,
    private val clock: Clock,
    private val updatePropertyDraftUseCase: UpdatePropertyDraftUseCase,
    private val savePhotosToAppFilesUseCase: SavePhotosToAppFilesUseCase,
    private val setLastPhotoUriUseCase: SetLastPhotoUriUseCase,
    private val addPhotoToExistingPropertyDraftUseCase: AddPhotoToExistingPropertyDraftUseCase,
    private val addPhotosUseCase: AddPhotosUseCase,
    private val addPropertyUseCase: AddPropertyUseCase,
    private val updatePropertyUseCase: UpdatePropertyUseCase,
    private val updatePhotosForPropertyIdUseCase: UpdatePhotosForPropertyIdUseCase
) : ViewModel() {

    private companion object {
        private const val TAG = "AddPropertyViewModel"
        private val ADDRESS_INPUT_DELAY = 300.milliseconds
        private val SAVE_DELAY = 3.seconds
    }

    private val propertyFormMutableSharedFlow: MutableSharedFlow<PropertyFormEntity> = MutableSharedFlow(replay = 1)
    private var existingDraftInitialState: PropertyFormEntity? = null

    private val currentAddressInputMutableStateFlow: MutableStateFlow<Pair<String, Boolean>?> = MutableStateFlow(null)
    private val addressPredictionsFlow: Flow<AutocompleteWrapper?> = currentAddressInputMutableStateFlow
        .transformLatest { addressData ->
            if (addressData == null) {
                emit(null)
            } else {
                val (input, fromUser) = addressData
                if (fromUser) {
                    if (input.length < 5) {
                        emit(null)
                    } else {
                        delay(ADDRESS_INPUT_DELAY)
                        emit(getAddressPredictionsUseCase.invoke(input))
                    }
                }
            }
        }
    private val errorsMutableStateFlow: MutableStateFlow<PropertyFormErrors> = MutableStateFlow(PropertyFormErrors())

    private val draftId: Long = requireNotNull(savedStateHandle.get<Long>("draftId")) {
        "No property id passed as parameter !"
    }

    private val isNewDraft = requireNotNull(savedStateHandle.get<Boolean>("isNewDraft")) {
        "No information about new draft passed as parameter !"
    }

    private val isExistingProperty = savedStateHandle.get<Boolean>("isExistingProperty") ?: false

    private val photosFlow: Flow<List<PhotoEntity>> = if (isExistingProperty) {
        getPhotosForExistingPropertyDraftIdUseCase.invoke(draftId)
    } else {
        getPhotosForPropertyIdUseCase.invoke(draftId)
    }
    private var photosList: List<PhotoEntity> = emptyList()
    private var isFormLoaded = false
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
        if (saveJob?.isActive == true) {
            saveJob?.cancel()
        }

        if (isFormLoaded) {
            setSaveJob(propertyForm, currency, euroRate, surfaceUnit)
        } else {
            isFormLoaded = true

            if (isNewDraft) {
                setSaveJob(propertyForm, currency, euroRate, surfaceUnit)
            }
        }
    }

    init {
        if (!isNewDraft && !isExistingProperty) {
            navigateUseCase.invoke(To.CloseDraftListInBackground)
        }
    }

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        if (latestValue == null) {
            if (isNewDraft) {
                propertyFormMutableSharedFlow.tryEmit(initPropertyForm(draftId))
            } else {
                viewModelScope.launch {
                    val draft = getPropertyDraftByIdUseCase.invoke(draftId)
                    if (draft != null) {
                        propertyFormMutableSharedFlow.tryEmit(draft)

                        existingDraftInitialState = draft
                    }
                }
            }
        }

        combine(
            propertyInformationFlow,
            addressPredictionsFlow,
            photosFlow,
            errorsMutableStateFlow
        ) { (propertyForm, currency, euroRate, surfaceUnit), addressPredictions, photos, errors ->
            updatePhotos(photos, propertyForm)

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
                surfaceUnit = surfaceUnit,
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
                    propertyForm.featuredPhotoId,
                    { clickedPhotoIndex -> navigateUseCase.invoke(To.DraftPhotos(clickedPhotoIndex, isExistingProperty)) },
                    { photoId ->
                        if (propertyForm.featuredPhotoId != photoId) {
                            propertyFormMutableSharedFlow.tryEmit(
                                propertyForm.copy(featuredPhotoId = photoId)
                            )
                        }
                    },
                    { id, uri ->
                        viewModelScope.launch {
                            if (isExistingProperty) {
                                deletePhotosForExistingPropertyDraftUseCase.invoke(listOf(id), listOf(uri))
                            } else {
                                deletePhotosUseCase.invoke(listOf(id), listOf(uri))
                            }
                        }
                    },
                    { id, description ->
                        viewModelScope.launch {
                            navigateUseCase.invoke(To.EditPhotoDescription(id, description, isExistingProperty))
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
                isPhotosDescriptionsErrorVisible = errors.isPhotosDescriptionsErrorVisible
            )
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<AddPropertyViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.HideAddressSuggestions -> emit(Event(AddPropertyViewAction.HideSuggestions))
                is To.Camera -> emit(Event(AddPropertyViewAction.OpenCamera(it.propertyId, it.isExistingProperty)))
                is To.ImportedPhotoPreview -> emit(Event(AddPropertyViewAction.ShowImportedPhotoPreview(it.propertyId, it.isExistingProperty)))
                is To.EditPhotoDescription -> emit(Event(AddPropertyViewAction.EditPhotoDescription(it.photoId, it.description, it.isExistingProperty)))
                is To.DraftPhotos -> emit(Event(AddPropertyViewAction.ShowPhotosViewer(draftId, it.clickedPhotoIndex, it.isExistingProperty)))
                is To.SaveDraftDialog -> emit(Event(AddPropertyViewAction.SaveDraftDialog))
                is To.CloseAddProperty -> emit(Event(AddPropertyViewAction.CloseDialog))
                is To.AppSettings -> emit(Event(AddPropertyViewAction.OpenAppSettings))
                is To.Toast -> {
                    if (it.message == NativeText.Resource(R.string.toast_selected_address_details_error) ||
                        it.message == NativeText.Resource(R.string.toast_selected_address_details_failure) ||
                        it.message == NativeText.Resource(R.string.toast_selected_address_no_results) ||
                        it.message == NativeText.Resource(R.string.toast_draft_discarded) ||
                        it.message == NativeText.Resource(R.string.toast_saving_draft) ||
                        it.message == NativeText.Resource(R.string.toast_draft_saved) ||
                        it.message == NativeText.Resource(R.string.toast_draft_automatically_saved)
                    ) {
                        emit(Event(AddPropertyViewAction.ShowToast(it.message)))
                    }
                }
                else -> Unit
            }
        }
    }

    private fun setSaveJob(
        propertyForm: PropertyFormEntity,
        currency: AppCurrency,
        euroRate: CurrencyRateWrapper,
        surfaceUnit: SurfaceUnit
    ) {
        saveJob = viewModelScope.launch {
            delay(SAVE_DELAY)

            val priceInDollars = propertyForm.priceFromUser?.let {
                if (currency == AppCurrency.EUR) {
                    it.fromEurosToDollars(euroRate.currencyRateEntity.rate)
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
            Log.d(
                "test",
                "saveDraft: $propertyForm\npriceInDollars: $priceInDollars\nsurfaceInMeters: $surfaceInMeters"
            )
            viewModelScope.launch {
                updatePropertyDraftUseCase.invoke(
                    draftId,
                    propertyForm.copy(
                        referencePrice = priceInDollars,
                        referenceSurface = surfaceInMeters
                    )
                )
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
        autoCompleteAddress = PropertyFormAddress(),
        address = PropertyFormAddress(),
        description = null,
        photos = emptyList(),
        featuredPhotoId = null,
        agent = null,
        lastUpdate = ZonedDateTime.now(clock).toLocalDateTime()
    )

    private fun updatePhotos(
        photos: List<PhotoEntity>,
        propertyForm: PropertyFormEntity
    ) {
        if (photosList != photos) {
            if ((photosList.isEmpty() && photos.size == 1) ||
                (photos.find { it.id == propertyForm.featuredPhotoId } == null && photos.isNotEmpty())
            ) {
                propertyFormMutableSharedFlow.tryEmit(
                    propertyForm.copy(featuredPhotoId = photos.first().id)
                )
            }

            photosList = photos
        }
    }

    private fun formatDate(localDate: LocalDate?): NativeText? = localDate?.let {
        NativeText.Date(R.string.date_format, it)
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
        return when (surfaceUnit) {
            SurfaceUnit.FEET -> {
                NativeText.Multi(
                    listOf(
                        NativeText.Argument(
                            R.string.add_property_label_surface,
                            NativeText.Resource(surfaceUnit.unitSymbol)
                        ),
                        NativeText.Simple("*")
                    )
                )
            }

            else -> {
                NativeText.Argument(
                    R.string.add_property_label_surface,
                    NativeText.Resource(surfaceUnit.unitSymbol)
                )
            }
        }
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
                autoCompleteAddress = PropertyFormAddress(),
                address = PropertyFormAddress()
            )
        )
    }

    fun onPropertyTypeChanged(checkedId: Int) {
        Log.d("test", "onPropertyTypeChanged: $checkedId")
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
        Log.d("test", "onForSaleSinceDateChanged: $date")
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

    fun onPriceChanged(price: BigDecimal?) {
        Log.d("test", "onPriceChanged: $price")
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(
                priceFromUser = price,
                referencePrice = price
            )
        )

        if (price != null) {
            errorsMutableStateFlow.update {
                it.copy(priceError = null)
            }
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
        Log.d("test", "onAddressChanged: $address")
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
        val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
        if (propertyFormReplayCache.autoCompleteAddress != PropertyFormAddress()
            && formatAddress(propertyFormReplayCache.address) != address
        ) {
            resetAddressFields()
        }
    }

    fun onAddressTextCleared() {
        isAddressTextCleared = true
        currentAddressInputMutableStateFlow.value = null

    }

    fun onComplementaryAddressChanged(complementaryAddress: String) {
        Log.d("test", "onComplementaryAddressChanged: $complementaryAddress")
        val parsedComplementaryAddress = complementaryAddress.ifBlank { null }
        val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormReplayCache.copy(
                address = propertyFormReplayCache.address?.copy(complementaryAddress = parsedComplementaryAddress)
            )
        )
    }

    fun onComplementaryAddressTextCleared() {
        val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormReplayCache.copy(
                address = propertyFormReplayCache.address?.copy(complementaryAddress = null)
            )
        )
    }

    fun onDescriptionChanged(description: String) {
        Log.d("test", "onDescriptionChanged: $description")
        val parsedDescription = description.ifBlank { null }
        propertyFormMutableSharedFlow.tryEmit(
            propertyFormMutableSharedFlow.replayCache.first().copy(description = parsedDescription)
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
        navigateUseCase.invoke(To.Camera(draftId, isExistingProperty))
    }

    fun onOpenAppSettingsClicked() {
        navigateUseCase.invoke(To.AppSettings)
    }

    fun closeDialog() {
        val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
        if ((isNewDraft && isFormEmpty(propertyFormReplayCache)) ||
            (isExistingProperty && existingDraftInitialState == propertyFormReplayCache)
        ) {
            dropDraft()
        } else if (!isNewDraft && existingDraftInitialState == propertyFormReplayCache) {
            navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_draft_automatically_saved)))
            navigateUseCase.invoke(To.CloseAddProperty)
        } else {
            navigateUseCase.invoke(To.SaveDraftDialog)
        }
    }

    private fun isFormEmpty(propertyFormReplayCache: PropertyFormEntity): Boolean {
        return propertyFormReplayCache.type == null &&
                propertyFormReplayCache.forSaleSince == null &&
                propertyFormReplayCache.dateOfSale == null &&
                propertyFormReplayCache.priceFromUser == null &&
                propertyFormReplayCache.surfaceFromUser == null &&
                propertyFormReplayCache.rooms == null &&
                propertyFormReplayCache.bathrooms == null &&
                propertyFormReplayCache.bedrooms == null &&
                propertyFormReplayCache.pointsOfInterest!!.isEmpty() &&
                propertyFormReplayCache.address == PropertyFormAddress() &&
                propertyFormReplayCache.autoCompleteAddress == PropertyFormAddress() &&
                propertyFormReplayCache.description == null &&
                photosList.isEmpty()
    }

    fun dropDraft() {
        viewModelScope.launch {
            deletePropertyDraftWithPhotosUseCase.invoke(draftId, photosList, isExistingProperty)
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
            navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_draft_saved)))
            navigateUseCase.invoke(To.CloseAddProperty)
        }
    }

    fun onGalleryPhotosSelected(photos: List<String>) {
        viewModelScope.launch {
            val savedPhotosList = if (isExistingProperty) {
                savePhotosToAppFilesUseCase.invoke(photos, true)
            } else {
                savePhotosToAppFilesUseCase.invoke(photos, false)
            }
            // If only one photo, opens it in photo preview
            if (savedPhotosList != null) {
                if (savedPhotosList.size == 1) {
                    setLastPhotoUriUseCase.invoke(savedPhotosList.first())
                    navigateUseCase.invoke(To.ImportedPhotoPreview(draftId, isExistingProperty))
                }
                // If multiple, adds them and lets user manually edit their description
                else {
                    if (isExistingProperty) {
                        addPhotoToExistingPropertyDraftUseCase.invoke(draftId, savedPhotosList, "")
                    } else {
                        addPhotosUseCase.invoke(draftId, savedPhotosList, "")
                    }
                    errorsMutableStateFlow.update {
                        it.copy(isPhotosDescriptionsErrorVisible = true)
                    }
                }
            }
        }
    }

    fun onDoneButtonClicked() {
        val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
        if (isExistingProperty) {
            if (isFormValid()) {
                if (existingDraftInitialState == propertyFormReplayCache) {
                    viewModelScope.launch {
                        deletePropertyDraftWithPhotosUseCase.invoke(draftId, photosList, true)
                        navigateUseCase.invoke(To.CloseAddProperty)
                    }
                    return
                } else {
                    val featuredPhotoIndex = photosList.indexOfFirst { it.id == propertyFormReplayCache.featuredPhotoId }
                    viewModelScope.launch {
                        val updatedPhotos = updatePhotosForPropertyIdUseCase.invoke(draftId, photosList)
                        if (updatedPhotos != null) {
                            val updatedProperty = updatePropertyUseCase.invoke(
                                mapFormToPropertyEntity(
                                    propertyFormReplayCache.copy(featuredPhotoId = updatedPhotos[featuredPhotoIndex])
                                )
                            )
                            if (updatedProperty > 0) {
                                navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_property_updated)))
                                deletePropertyDraftWithPhotosUseCase.invoke(draftId, photosList, true)
                                navigateUseCase.invoke(To.CloseAddProperty)
                            }
                        } else {
                            navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_error_updating_property)))
                        }
                    }
                }
            }

        } else {
            if (isFormValid()) {
                viewModelScope.launch {
                    val newPropertyId = addPropertyUseCase.invoke(mapFormToPropertyEntity(propertyFormReplayCache))
                    if (newPropertyId != null) {
                        navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_property_added)))
                        deletePropertyDraftWithPhotosUseCase.invoke(draftId, photosList, true)
                        navigateUseCase.invoke(To.CloseAddProperty)
                    } else {
                        navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_error_add_property)))
                    }
                }
            }
        }
    }

    private fun mapFormToPropertyEntity(propertyForm: PropertyFormEntity) = PropertyEntity(
        id = propertyForm.id,
        type = propertyForm.type!!.name,
        forSaleSince = propertyForm.forSaleSince!!,
        saleDate = propertyForm.dateOfSale,
        price = propertyForm.priceFromUser ?: propertyForm.referencePrice!!,
        surface = propertyForm.surfaceFromUser ?: propertyForm.referenceSurface!!,
        rooms = propertyForm.rooms!!,
        bathrooms = propertyForm.bathrooms ?: BigDecimal.ZERO,
        bedrooms = propertyForm.bedrooms ?: BigDecimal.ZERO,
        amenities = propertyForm.pointsOfInterest!!,
        address = PropertyAddressEntity(
            streetNumber = propertyForm.autoCompleteAddress!!.streetNumber!!,
            route = propertyForm.autoCompleteAddress.route!!,
            complementaryAddress = propertyForm.autoCompleteAddress.complementaryAddress,
            zipcode = propertyForm.autoCompleteAddress.zipcode!!,
            city = propertyForm.autoCompleteAddress.city!!,
            state = propertyForm.autoCompleteAddress.state!!,
            country = propertyForm.autoCompleteAddress.country!!,
            latitude = propertyForm.autoCompleteAddress.latitude!!,
            longitude = propertyForm.autoCompleteAddress.longitude!!
        ),
        description = propertyForm.description!!,
        photos = propertyForm.photos!!,
        featuredPhotoId = propertyForm.featuredPhotoId,
        agent = propertyForm.agent ?: "John Doe", // TODO: get agent name from settings
        entryDate = ZonedDateTime.now(clock).toLocalDateTime()
    )

    private fun isFormValid(): Boolean {
        val propertyFormReplayCache = propertyFormMutableSharedFlow.replayCache.first()
        var isFormValid = true

        if (propertyFormReplayCache.type == null) {
            errorsMutableStateFlow.update {
                it.copy(isTypeErrorVisible = true)
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(isTypeErrorVisible = false)
            }
        }

        if (propertyFormReplayCache.forSaleSince == null) {
            errorsMutableStateFlow.update {
                it.copy(forSaleSinceError = NativeText.Resource(R.string.add_property_error_date_required))
            }
            isFormValid = false
        } else if (propertyFormReplayCache.isSold == true &&
            propertyFormReplayCache.dateOfSale != null &&
            propertyFormReplayCache.forSaleSince.isAfter(propertyFormReplayCache.dateOfSale)
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

        if (propertyFormReplayCache.isSold == true) {
            if (propertyFormReplayCache.dateOfSale == null) {
                errorsMutableStateFlow.update {
                    it.copy(dateOfSaleError = NativeText.Resource(R.string.add_property_error_date_required))
                }
                isFormValid = false
            } else if (propertyFormReplayCache.forSaleSince != null &&
                propertyFormReplayCache.dateOfSale.isBefore(propertyFormReplayCache.forSaleSince)
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

        if (propertyFormReplayCache.priceFromUser == null &&
            propertyFormReplayCache.referencePrice == null
        ) {
            errorsMutableStateFlow.update {
                it.copy(priceError = NativeText.Resource(R.string.add_property_error_price_required))
            }
            isFormValid = false
        } else if (
            propertyFormReplayCache.priceFromUser != null &&
            propertyFormReplayCache.priceFromUser == BigDecimal.ZERO ||
            propertyFormReplayCache.referencePrice != null &&
            propertyFormReplayCache.referencePrice == BigDecimal.ZERO
        ) {
            errorsMutableStateFlow.update {
                it.copy(priceError = NativeText.Resource(R.string.add_property_error_invalid_price))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(priceError = null)
            }
        }

        if ((propertyFormReplayCache.referenceSurface == null &&
                    propertyFormReplayCache.surfaceFromUser == null) ||
            (propertyFormReplayCache.referenceSurface == BigDecimal.ZERO &&
                    propertyFormReplayCache.surfaceFromUser == null) ||
            propertyFormReplayCache.surfaceFromUser == BigDecimal.ZERO
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

        if (propertyFormReplayCache.rooms == null ||
            propertyFormReplayCache.rooms == BigDecimal.ZERO
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

        if (propertyFormReplayCache.autoCompleteAddress == PropertyFormAddress()) {
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

        if (propertyFormReplayCache.description.isNullOrEmpty()) {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = NativeText.Resource(R.string.add_property_error_description_required))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(descriptionError = null)
            }
        }

        var arePhotosDescriptionsValid = true
        photosList.forEach { photo ->
            if (photo.description.isEmpty() && arePhotosDescriptionsValid) {
                isFormValid = false
                arePhotosDescriptionsValid = false
                return@forEach
            }
        }
        errorsMutableStateFlow.update {
            it.copy(isPhotosDescriptionsErrorVisible = !arePhotosDescriptionsValid)
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
        val descriptionError: NativeText? = null,
        val isPhotosDescriptionsErrorVisible: Boolean = false
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
