package com.bakjoul.realestatemanager.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.GetPropertyByIdUseCase
import com.bakjoul.realestatemanager.domain.property.SetPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getCurrentPropertyIdUseCase: GetCurrentPropertyIdUseCase,
    private val getPropertyByIdUseCase: GetPropertyByIdUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val setPropertyIdUseCase: SetPropertyIdUseCase,
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
                            surface = propertyEntity.surface.toString(),
                            rooms = propertyEntity.rooms.toString(),
                            bedrooms = propertyEntity.bedrooms.toString(),
                            bathrooms = propertyEntity.bathrooms.toString(),
                            address = propertyEntity.address,
                            appartment = propertyEntity.appartment,
                            city = propertyEntity.city,
                            zipcode = propertyEntity.zipcode.toString(),
                            country = propertyEntity.country
                        )
                    )
                }
            }
        }
    }

    fun resetPropertyId() {
        setPropertyIdUseCase.invoke(null)
    }

    fun onResume() {
        refreshOrientationUseCase.invoke()
    }
}
