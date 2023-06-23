package com.bakjoul.realestatemanager.ui.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.currency_rate.GetCachedEuroRateUseCase
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val application: Application,
    private val getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCachedEuroRateUseCase: GetCachedEuroRateUseCase,
    isTabletUseCase: IsTabletUseCase
) : ViewModel() {

    private companion object {
        private const val STATIC_MAP_SIZE = "160x160"
        private const val STATIC_MAP_ZOOM = "17"
    }

    private val locale = Locale.getDefault()
    private val formatter: DateTimeFormatter = if (locale.language == "fr") {
        DateTimeFormatter.ofPattern("d/MM/yy", locale)
    } else {
        DateTimeFormatter.ofPattern("M/d/yy", locale)
    }

    val isTabletLiveData: LiveData<Boolean> = isTabletUseCase.invoke().asLiveData()

    val detailsLiveData: LiveData<DetailsViewState> = liveData {
        combine(
            getCurrentPropertyUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            getCachedEuroRateUseCase.invoke()
        ) { property, currency, euroRate ->
            DetailsViewState(
                photoUrl = property.photos.first().url,
                type = property.type,
                price = formatPrice(property.price, currency, euroRate.rate),
                isSold = property.soldDate != null,
                city = property.city,
                sale_status = getSaleStatus(property.soldDate, property.entryDate),
                description = property.description,
                surface = formatSurface(property.surface),
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
                location = formatLocation(
                    property.address,
                    formatApartment(property.apartment),
                    property.city,
                    property.zipcode,
                    property.country
                ),
                media = mapPhotoEntities(property.photos),
                staticMapUrl = getMapUrl(property.address, property.city, property.country)
            )

        }.collect { viewState ->
            emit(viewState)
        }
    }

    private fun formatPrice(price: Double, currency: AppCurrency, euroRate: Double): String {
        val numberFormat = NumberFormat.getNumberInstance()

        val formattedPrice = when (currency) {
            AppCurrency.USD -> {
                numberFormat.currency = Currency.getInstance(Locale.US)
                numberFormat.maximumFractionDigits = 0
                "$" + numberFormat.format(price)
            }

            AppCurrency.EUR -> {
                val convertedPrice = price / euroRate
                numberFormat.currency = Currency.getInstance(Locale.FRANCE)
                numberFormat.maximumFractionDigits = 0
                numberFormat.format(convertedPrice).replace(",", ".") + " €"
            }
        }

        return formattedPrice
    }

    private fun getSaleStatus(soldDate: LocalDate?, entryDate: LocalDate): String {
        return if (soldDate != null) {
            application.getString(R.string.details_sold_on) + soldDate.format(formatter)
        } else {
            application.getString(R.string.details_for_sale_since) + entryDate.format(formatter)
        }
    }

    private fun mapPhotoEntities(photoEntities: List<PhotoEntity>): List<DetailsMediaItemViewState> {
        return photoEntities.map { photoEntity ->
            DetailsMediaItemViewState(
                id = photoEntity.id,
                url = photoEntity.url,
                description = photoEntity.description,
                onPhotoClicked = EquatableCallback { }
            )
        }
    }

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }

    private fun formatSurface(surface: Int): String {
        return "$surface m²"
    }

    private fun formatLocation(
        address: String,
        apartment: String,
        city: String,
        zipcode: String,
        country: String
    ): String {
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

    private fun getMapUrl(address: String, city: String, country: String): String {
        val formattedAddress = formatAddress("$address,$city,$country")
        return "https://maps.googleapis.com/maps/api/staticmap?&size=$STATIC_MAP_SIZE&zoom=$STATIC_MAP_ZOOM&markers=$formattedAddress&key=${BuildConfig.MAPS_API_KEY}"
    }

    private fun formatAddress(address: String) = address.replace(" ", "%20")
}
