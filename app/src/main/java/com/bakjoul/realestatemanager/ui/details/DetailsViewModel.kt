package com.bakjoul.realestatemanager.ui.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_property.ResetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application,
    private val getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val resetCurrentPropertyIdUseCase: ResetCurrentPropertyIdUseCase,
    private val setCurrentPhotoIdUseCase: SetCurrentPhotoIdUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private companion object {
        private const val STATIC_MAP_SIZE = "250x250"
        private const val STATIC_MAP_ZOOM = "17"
    }

    val detailsLiveData: LiveData<DetailsViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(
            getCurrentPropertyUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke()) },
            getCurrentSurfaceUnitUseCase.invoke()
        ) { property, currency, euroRateWrapper, surfaceUnit ->
            val details = property.propertyEntity
            val formattedSurface = formatSurface(details.surface, surfaceUnit)

            DetailsViewState(
                mainPhotoUrl = "",//property.photos.first().url,
                type = details.type,
                price = formatPrice(details.price, currency, euroRateWrapper.currencyRateEntity.rate),
                isSold = details.soldDate != null,
                city = details.city,
                sale_status = getSaleStatus(details.soldDate, details.entryDate),
                description = details.description,
                surface = "${formattedSurface.first} ${formattedSurface.second}",
                rooms = details.rooms.toString(),
                bedrooms = details.bedrooms.toString(),
                bathrooms = details.bathrooms.toString(),
                poiSchool = details.poiSchool,
                poiStore = details.poiStore,
                poiPark = details.poiPark,
                poiRestaurant = details.poiRestaurant,
                poiHospital = details.poiHospital,
                poiBus = details.poiBus,
                poiSubway = details.poiSubway,
                poiTramway = details.poiTramway,
                poiTrain = details.poiTrain,
                poiAirport = details.poiAirport,
                location = formatLocation(details.address, formatApartment(details.apartment), details.city, details.zipcode, details.country),
                medias = emptyList(),//mapPhotosToMediaItemViewStates(property.photos),
                clipboardAddress = getClipboardAddress(details.address, details.city, details.country),
                staticMapUrl = getMapUrl(details.address, details.city, details.country),
                mapsAddress = getAddress(details.address, details.city, details.country)
            )
        }.collect {
            emit(it)
        }
    }


    private val locale = Locale.getDefault()
    private val formatter: DateTimeFormatter = if (locale.language == "fr") {
        DateTimeFormatter.ofPattern("d/MM/yy", locale)
    } else {
        DateTimeFormatter.ofPattern("M/d/yy", locale)
    }

    private fun getSaleStatus(soldDate: LocalDate?, entryDate: LocalDate): String {
        return if (soldDate != null) {
            application.getString(R.string.property_sold_on) + soldDate.format(formatter)
        } else {
            application.getString(R.string.property_for_sale_since) + entryDate.format(formatter)
        }
    }

    private fun mapPhotosToMediaItemViewStates(photoEntities: List<PhotoEntity>): List<DetailsMediaItemViewState> {
        return photoEntities.map { photoEntity ->
            DetailsMediaItemViewState(
                id = photoEntity.id,
                url = photoEntity.url,
                description = photoEntity.description,
                onPhotoClicked = EquatableCallback {
                    setCurrentPhotoIdUseCase.invoke(photoEntity.id.toInt())
                    navigateUseCase.invoke(To.PhotosDialog)
                }
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
        return "https://maps.googleapis.com/maps/api/staticmap?&size=$STATIC_MAP_SIZE&zoom=$STATIC_MAP_ZOOM&markers=$formattedAddress&key=${BuildConfig.GOOGLE_API_KEY}"
    }

    private fun getAddress(address: String, city: String, country: String): String {
        return formatAddress("$address $city $country")
    }

    private fun formatAddress(address: String) = address.replace(" ", "%20")

    fun onBackButtonPressed() {
        resetCurrentPropertyIdUseCase.invoke()
        navigateUseCase.invoke(To.CloseDetails)
    }
}
