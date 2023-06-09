package com.bakjoul.realestatemanager.ui.details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    isTabletUseCase: IsTabletUseCase
) : ViewModel() {

    val isTabletLiveData: LiveData<Boolean> = isTabletUseCase.invoke().asLiveData()

    val detailsLiveData: LiveData<DetailsViewState> = liveData {
        getCurrentPropertyUseCase.invoke().collect { propertyEntity ->
            Log.d("test", "details property: $propertyEntity")
            emit(
                DetailsViewState(
                    description = propertyEntity.description,
                    surface = formatSurface(propertyEntity.surface),
                    rooms = propertyEntity.rooms.toString(),
                    bedrooms = propertyEntity.bedrooms.toString(),
                    bathrooms = propertyEntity.bathrooms.toString(),
                    location = formatLocation(
                        propertyEntity.address,
                        formatApartment(propertyEntity.apartment),
                        propertyEntity.city,
                        propertyEntity.zipcode,
                        propertyEntity.country
                    ),
                    media = mapPhotoEntities(propertyEntity.photos),
                    map = ""
                )
            )
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
}
