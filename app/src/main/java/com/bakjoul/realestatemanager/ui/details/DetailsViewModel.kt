package com.bakjoul.realestatemanager.ui.details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val application: Application,
    private val getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    isTabletUseCase: IsTabletUseCase
) : ViewModel() {

    val isTabletLiveData: LiveData<Boolean> = isTabletUseCase.invoke().asLiveData()

    val detailsLiveData: LiveData<DetailsViewState> = liveData {
        getCurrentPropertyUseCase.invoke().collect { propertyEntity ->
            emit(
                DetailsViewState(
                    photoUrl = propertyEntity.photos.first().url,
                    type = propertyEntity.type,
                    price = propertyEntity.price.toString(),
                    isSold = propertyEntity.soldDate != null,
                    city = propertyEntity.city,
                    sale_status = getSaleStatus(propertyEntity.soldDate, propertyEntity.entryDate),
                    description = propertyEntity.description,
                    surface = formatSurface(propertyEntity.surface),
                    rooms = propertyEntity.rooms.toString(),
                    bedrooms = propertyEntity.bedrooms.toString(),
                    bathrooms = propertyEntity.bathrooms.toString(),
                    poiSchool = propertyEntity.poiSchool,
                    poiStore = propertyEntity.poiStore,
                    poiPark = propertyEntity.poiPark,
                    poiRestaurant = propertyEntity.poiRestaurant,
                    poiHospital = propertyEntity.poiHospital,
                    poiBus = propertyEntity.poiBus,
                    poiSubway = propertyEntity.poiSubway,
                    poiTramway = propertyEntity.poiTramway,
                    poiTrain = propertyEntity.poiTrain,
                    poiAirport = propertyEntity.poiAirport,
                    location = formatLocation(
                        propertyEntity.address,
                        formatApartment(propertyEntity.apartment),
                        propertyEntity.city,
                        propertyEntity.zipcode,
                        propertyEntity.country
                    ),
                    media = mapPhotoEntities(propertyEntity.photos),
                    staticMapUrl = getMapUrl(propertyEntity.address, propertyEntity.city, propertyEntity.country)
                )
            )
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
        return "https://maps.googleapis.com/maps/api/staticmap?&size=160x160&zoom=17&markers=$formattedAddress&key=${BuildConfig.MAPS_API_KEY}"
    }

    private fun formatAddress(address: String) = address.replace(" ", "%20")
}
