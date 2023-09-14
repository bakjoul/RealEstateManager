package com.bakjoul.realestatemanager.ui.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_property.ResetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.common_model.PhotoItemViewState
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
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
            val formattedSurface = formatSurface(property.surface, surfaceUnit)

            DetailsViewState(
                mainPhotoUrl = property.photos.first().url,
                type = property.type,
                price = formatPrice(property.price, currency, euroRateWrapper.currencyRateEntity.rate),
                isSold = property.saleDate != null,
                city = property.fullAddress.city,
                sale_status = getSaleStatus(property.saleDate, property.entryDate),
                description = property.description,
                surface = "${formattedSurface.first} ${formattedSurface.second}",
                rooms = property.rooms.toString(),
                bedrooms = property.bedrooms.toString(),
                bathrooms = property.bathrooms.toString(),
                poiSchool = property.amenities.contains(PropertyPoiEntity.School),
                poiStore = property.amenities.contains(PropertyPoiEntity.Store),
                poiPark = property.amenities.contains(PropertyPoiEntity.Park),
                poiRestaurant = property.amenities.contains(PropertyPoiEntity.Restaurant),
                poiHospital = property.amenities.contains(PropertyPoiEntity.Hospital),
                poiBus = property.amenities.contains(PropertyPoiEntity.Bus),
                poiSubway = property.amenities.contains(PropertyPoiEntity.Subway),
                poiTramway = property.amenities.contains(PropertyPoiEntity.Tramway),
                poiTrain = property.amenities.contains(PropertyPoiEntity.Train),
                poiAirport = property.amenities.contains(PropertyPoiEntity.Airport),
                location = formatLocation(property.fullAddress.address, formatApartment(property.fullAddress.apartment), property.fullAddress.city, property.fullAddress.zipcode, property.fullAddress.country),
                medias = mapPhotosToItemViewStates(property.photos),
                clipboardAddress = getClipboardAddress(property.fullAddress.address, property.fullAddress.city, property.fullAddress.country),
                staticMapUrl = getMapUrl(property.fullAddress.address, property.fullAddress.city, property.fullAddress.country),
                mapsAddress = getAddress(property.fullAddress.address, property.fullAddress.city, property.fullAddress.country)
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

    private fun mapPhotosToItemViewStates(photoEntities: List<PhotoEntity>): List<PhotoItemViewState> {
        return photoEntities.mapIndexed { index, photoEntity ->
            PhotoItemViewState(
                id = index.toLong(),
                url = photoEntity.url,
                description = photoEntity.description,
                onPhotoClicked = EquatableCallback {
                    setCurrentPhotoIdUseCase.invoke(index)
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
