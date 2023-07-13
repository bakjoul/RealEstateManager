package com.bakjoul.realestatemanager.ui.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.currency_rate.GetCachedEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdChannelUseCase
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val application: Application,
    private val getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCachedEuroRateUseCase: GetCachedEuroRateUseCase,
    private val setCurrentPhotoIdUseCase: SetCurrentPhotoIdUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    isTabletUseCase: IsTabletUseCase,
    getCurrentPhotoIdChannelUseCase: GetCurrentPhotoIdChannelUseCase
) : ViewModel() {

    private companion object {
        private const val STATIC_MAP_SIZE = "250x250"
        private const val STATIC_MAP_ZOOM = "17"
    }

    val detailsLiveData: LiveData<DetailsViewState> = liveData {
        combine(
            getCurrentPropertyUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            getCachedEuroRateUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke()
        ) { property, currency, euroRate, surfaceUnit ->
            DetailsViewState(
                mainPhotoUrl = property.photos.first().url,
                type = property.type,
                price = formatPrice(property.price, currency, euroRate.rate),
                isSold = property.soldDate != null,
                city = property.city,
                sale_status = getSaleStatus(property.soldDate, property.entryDate),
                description = property.description,
                surface = formatSurface(property.surface, surfaceUnit),
                rooms = property.rooms.toString(),
                bedrooms = property.bedrooms.toString(),
                bathrooms = property.bathrooms.toString(),
                poiSchool = property.poiSchool,
                poiStore = property.poiStore,
                poiPark = property.poiPark,
                poiRestaurant = property.poiRestaurant,
                poiHospital = property.poiHospital,
                poiBus = property.poiBus,
                poiSubway = property.poiSubway,
                poiTramway = property.poiTramway,
                poiTrain = property.poiTrain,
                poiAirport = property.poiAirport,
                location = formatLocation(property.address, formatApartment(property.apartment), property.city, property.zipcode, property.country),
                medias = mapPhotosToMediaItemViewStates(property.photos),
                clipboardAddress = getClipboardAddress(property.address, property.city, property.country),
                staticMapUrl = getMapUrl(property.address, property.city, property.country),
                mapsAddress = getAddress(property.address, property.city, property.country)
            )
        }.collect { viewState ->
            emit(viewState)
        }
    }

    val detailsViewActionLiveData: LiveData<Event<DetailsViewAction>> =
        combine(
            isTabletUseCase.invoke(),
            getCurrentPhotoIdChannelUseCase.invoke()
        ) { isTablet, currentPhotoId ->
            if (!isTablet && currentPhotoId != -1) {
                DetailsViewAction.DisplayPhotosDialog
            } else {
                null
            }
        }.filterNotNull().map {
            Event(it)
        }.asLiveData()


    private val locale = Locale.getDefault()
    private val formatter: DateTimeFormatter = if (locale.language == "fr") {
        DateTimeFormatter.ofPattern("d/MM/yy", locale)
    } else {
        DateTimeFormatter.ofPattern("M/d/yy", locale)
    }

    private fun getSaleStatus(soldDate: LocalDate?, entryDate: LocalDate): String {
        return if (soldDate != null) {
            application.getString(R.string.details_sold_on) + soldDate.format(formatter)
        } else {
            application.getString(R.string.details_for_sale_since) + entryDate.format(formatter)
        }
    }

    private fun mapPhotosToMediaItemViewStates(photoEntities: List<PhotoEntity>): List<DetailsMediaItemViewState> {
        return photoEntities.map { photoEntity ->
            DetailsMediaItemViewState(
                id = photoEntity.id,
                url = photoEntity.url,
                description = photoEntity.description,
                onPhotoClicked = EquatableCallback { setCurrentPhotoIdUseCase.invoke(photoEntity.id.toInt()) }
            )
        }
    }

    private fun formatLocation(address: String, apartment: String, city: String, zipcode: String, country: String): String {
        val location = buildString {
            append(address)
            if (apartment.isNotEmpty()) {
                append("\n$apartment")
            }
            append("\n$city\n$zipcode\n$country")
        }
        return location
    }

    private fun formatApartment(apartment: String): String {
        return if (apartment.isNotEmpty()) {
            "Apt $apartment"
        } else {
            ""
        }
    }

    private fun getClipboardAddress(address: String, city: String, country: String): String =
        "$address $city $country"

    private fun getMapUrl(address: String, city: String, country: String): String {
        val formattedAddress = formatAddress("$address,$city,$country")
        return "https://maps.googleapis.com/maps/api/staticmap?&size=$STATIC_MAP_SIZE&zoom=$STATIC_MAP_ZOOM&markers=$formattedAddress&key=${BuildConfig.MAPS_API_KEY}"
    }

    private fun getAddress(address: String, city: String, country: String): String {
        return formatAddress("$address $city $country")
    }

    private fun formatAddress(address: String) = address.replace(" ", "%20")

    fun onResume(isTablet: Boolean) = refreshOrientationUseCase.invoke(isTablet)
}
