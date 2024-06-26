package com.bakjoul.realestatemanager.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListMapper
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.SelectType
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_property.ResetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.main.SetClipboardToastStateUseCase
import com.bakjoul.realestatemanager.domain.main.SetEditErrorToastStateUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.CopyPhotosToPhotoDraftsUseCase
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.AddPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.DoesDraftExistForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.MapPropertyToPropertyFormUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurfaceValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val resetCurrentPropertyIdUseCase: ResetCurrentPropertyIdUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val setClipboardToastStateUseCase: SetClipboardToastStateUseCase,
    private val doesDraftExistForPropertyIdUseCase: DoesDraftExistForPropertyIdUseCase,
    private val copyPhotosToPhotoDraftsUseCase: CopyPhotosToPhotoDraftsUseCase,
    private val setEditErrorToastStateUseCase: SetEditErrorToastStateUseCase,
    private val addPropertyDraftUseCase: AddPropertyDraftUseCase,
    private val mapPropertyToPropertyFormUseCase: MapPropertyToPropertyFormUseCase
) : ViewModel() {

    private companion object {
        private const val STATIC_MAP_SIZE = "250x250"
        private const val STATIC_MAP_ZOOM = "17"
    }

    private var propertyEntity: PropertyEntity? = null

    val viewStateLiveData: LiveData<DetailsViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(
            getCurrentPropertyUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke()) },
            getCurrentSurfaceUnitUseCase.invoke()
        ) { property, currency, euroRateWrapper, surfaceUnit ->
            if (propertyEntity == null ||
                propertyEntity?.id != property.id ||
                propertyEntity?.featuredPhotoId != property.featuredPhotoId
            ) {
                propertyEntity = property
            }

            val parsedSurfaceValue = formatSurfaceValue(property.surface, surfaceUnit)
            DetailsViewState(
                featuredPhotoUrl = property.photos.find { it.id == property.featuredPhotoId }?.uri ?: "",
                type = formatType(property.type),
                price = formatPrice(property.price, currency, euroRateWrapper.currencyRateEntity.rate),
                isSold = property.saleDate != null,
                city = property.address.city,
                saleStatus = getSaleStatus(property.saleDate, property.entryDate),
                description = property.description,
                surface = formatSurface(parsedSurfaceValue, surfaceUnit),
                rooms = property.rooms.toString(),
                bedrooms = property.bedrooms.toString(),
                bathrooms = property.bathrooms.toString(),
                poiSchool = property.amenities.contains(PropertyPoiEntity.SCHOOL),
                poiStore = property.amenities.contains(PropertyPoiEntity.STORE),
                poiPark = property.amenities.contains(PropertyPoiEntity.PARK),
                poiRestaurant = property.amenities.contains(PropertyPoiEntity.RESTAURANT),
                poiHospital = property.amenities.contains(PropertyPoiEntity.HOSPITAL),
                poiBus = property.amenities.contains(PropertyPoiEntity.BUS),
                poiSubway = property.amenities.contains(PropertyPoiEntity.SUBWAY),
                poiTramway = property.amenities.contains(PropertyPoiEntity.TRAMWAY),
                poiTrain = property.amenities.contains(PropertyPoiEntity.TRAIN),
                poiAirport = property.amenities.contains(PropertyPoiEntity.AIRPORT),
                location = formatLocation(property.address.streetNumber, property.address.route, formatApartment(property.address.complementaryAddress), property.address.city, property.address.zipcode, property.address.country),
                medias = PhotoListMapper().map(
                    property.photos,
                    { SelectType.NOT_SELECTABLE },
                    property.featuredPhotoId,
                    { clickedPhotoIndex -> navigateUseCase.invoke(To.Photos(property.id, clickedPhotoIndex)) }
                ),
                clipboardAddress = getClipboardAddress(property.address.streetNumber, property.address.route, property.address.city, property.address.country),
                staticMapUrl = getMapUrl(property.address.streetNumber, property.address.route, property.address.city, property.address.country),
                mapsAddress = getAddress(property.address.streetNumber, property.address.route, property.address.city, property.address.country)
            )
        }.collect {
            emit(it)
        }
    }

    private fun formatType(type: String): NativeText = when (type) {
        PropertyTypeEntity.DUPLEX.name -> NativeText.Resource(PropertyTypeEntity.DUPLEX.typeName)
        PropertyTypeEntity.FLAT.name -> NativeText.Resource(PropertyTypeEntity.FLAT.typeName)
        PropertyTypeEntity.HOUSE.name -> NativeText.Resource(PropertyTypeEntity.HOUSE.typeName)
        PropertyTypeEntity.LOFT.name -> NativeText.Resource(PropertyTypeEntity.LOFT.typeName)
        PropertyTypeEntity.OTHER.name -> NativeText.Resource(PropertyTypeEntity.OTHER.typeName)
        PropertyTypeEntity.PENTHOUSE.name -> NativeText.Resource(PropertyTypeEntity.PENTHOUSE.typeName)
        else -> NativeText.Simple("")
    }

    private fun getSaleStatus(soldDate: LocalDate?, entryDate: LocalDateTime): NativeText {
        return if (soldDate != null) {
            NativeText.Argument(
                R.string.property_sold_on,
                NativeText.Date(
                    R.string.details_date_formatter,
                    soldDate
                )
            )
        } else {
            NativeText.Argument(
                R.string.property_for_sale_since,
                NativeText.Date(
                    R.string.details_date_formatter,
                    entryDate
                )
            )
        }
    }

    private fun formatSurface(formattedSurfaceValue: Int, surfaceUnit: SurfaceUnit): NativeText {
        return NativeText.Arguments(
            R.string.property_surface,
            listOf(
                formattedSurfaceValue,
                NativeText.Resource(surfaceUnit.unitSymbol)
            )
        )
    }

    private fun formatLocation(
        streetNumber: String,
        route: String,
        apartment: String,
        city: String,
        zipcode: String,
        country: String
    ): String {
        val location = buildString {
            append("$streetNumber $route")
            if (apartment.isNotEmpty()) {
                append("\n$apartment")
            }
            if (Locale.getDefault() == Locale.FRANCE) {
                append("\n$zipcode $city\n$country")
            } else {
                append("\n$city\n$zipcode\n$country")
            }
        }
        return location
    }

    private fun formatApartment(complementaryAddress: String?): String {
        return if (complementaryAddress != null) {
            "$complementaryAddress"
        } else {
            ""
        }
    }

    private fun getClipboardAddress(streetNumber: String, route: String, city: String, country: String): String =
        "$streetNumber $route $city $country"

    private fun getMapUrl(streetNumber: String, route: String, city: String, country: String): String {
        val formattedAddress = formatAddress("$streetNumber $route,$city,$country")
        return "https://maps.googleapis.com/maps/api/staticmap?&size=$STATIC_MAP_SIZE&zoom=$STATIC_MAP_ZOOM&markers=$formattedAddress&key=${BuildConfig.GOOGLE_API_KEY}"
    }

    private fun getAddress(streetNumber: String, route: String, city: String, country: String): String {
        return formatAddress("$streetNumber $route $city $country")
    }

    private fun formatAddress(address: String) = address.replace(" ", "%20")

    fun onBackButtonClicked() {
        resetCurrentPropertyIdUseCase.invoke()
        navigateUseCase.invoke(To.CloseDetails)
    }

    fun onLocationClicked() {
        setClipboardToastStateUseCase.invoke(true)
        navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.property_address_clipboard)))
    }

    fun onEditButtonClicked() {
        val property = propertyEntity ?: return

        viewModelScope.launch {
            if (doesDraftExistForPropertyIdUseCase.invoke(property.id)) {
                navigateUseCase.invoke(To.EditPropertyDraftAlertDialog(property))
            } else {
                if (property.photos.isNotEmpty()) {
                    val draftPhotos = copyPhotosToPhotoDraftsUseCase.invoke(property.id)
                    if (draftPhotos == null) {
                        setEditErrorToastStateUseCase.invoke(true)
                        navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_property_edit_error)))
                        return@launch
                    }
                    val featuredPhotoIndex = property.photos.indexOfFirst { it.id == property.featuredPhotoId }
                    addPropertyDraftUseCase.invoke(
                        mapPropertyToPropertyFormUseCase.invoke(
                            property.copy(
                                photos = draftPhotos,
                                featuredPhotoId = draftPhotos[featuredPhotoIndex].id
                            )
                        )
                    )
                } else {
                    addPropertyDraftUseCase.invoke(mapPropertyToPropertyFormUseCase.invoke(property))
                }

                navigateUseCase.invoke(To.EditProperty(property.id))
            }
        }
    }

    fun onDeleteButtonClicked() {
        val property = propertyEntity ?: return

        navigateUseCase.invoke(To.DeletePropertyAlertDialog(property))
    }
}
