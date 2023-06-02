package com.bakjoul.realestatemanager.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.current_property.GetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.current_property.SetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.GetPropertyByIdUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getCurrentPropertyIdUseCase: GetCurrentPropertyIdUseCase,
    private val getPropertyByIdUseCase: GetPropertyByIdUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
    isTabletUseCase: IsTabletUseCase,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    val isTabletLiveData: LiveData<Boolean> =
        isTabletUseCase.invoke().asLiveData(coroutineDispatcherProvider.io)

    val detailsLiveData: LiveData<DetailsViewState> = liveData {
        getCurrentPropertyIdUseCase.invoke().collect { propertyId ->
            if (propertyId != null) {
                val propertyEntity = getPropertyByIdUseCase.invoke(propertyId)
                if (propertyEntity != null) {
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
                            )
                        )
                    )
                }
            }
        }
    }

    // TODO Ask Nino if this is the right way to do it
    fun resetPropertyId() {
        setCurrentPropertyIdUseCase.invoke(null)
    }

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }

    private fun formatSurface(surface: Int): String {
        return "$surface m²"
    }

    private fun formatLocation(address: String, apartment: String, city: String, zipcode: Int, country: String): String {
        val location = buildString {
            append(address)
            if (apartment.isNotEmpty()) {
                append("\n$apartment")
            }
            append("\n$city $zipcode\n$country")
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
