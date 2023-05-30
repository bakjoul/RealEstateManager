package com.bakjoul.realestatemanager.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.data.ResourcesRepository
import com.bakjoul.realestatemanager.data.property.CurrentPropertyRepository
import com.bakjoul.realestatemanager.data.property.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val currentPropertyRepository: CurrentPropertyRepository,
    private val propertyRepository: PropertyRepository,
    private val resourcesRepository: ResourcesRepository
) : ViewModel() {

    val isTabletLiveData: LiveData<Boolean> = resourcesRepository.isTabletFlow().asLiveData()

    val detailsLiveData: LiveData<DetailsViewState> = liveData(Dispatchers.IO) {
        currentPropertyRepository.getCurrentPropertyId().collect { propertyId ->
            if (propertyId != null) {
                val propertyEntity = propertyRepository.getPropertyById(propertyId)
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

    fun onResume() {
        resourcesRepository.refreshOrientation()
    }
}
