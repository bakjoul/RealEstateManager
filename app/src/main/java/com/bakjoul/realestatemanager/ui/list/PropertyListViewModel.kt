package com.bakjoul.realestatemanager.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.data.property.CurrentPropertyIdRepository
import com.bakjoul.realestatemanager.data.property.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val currentPropertyIdRepository: CurrentPropertyIdRepository
) : ViewModel() {

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData(Dispatchers.IO) {
        propertyRepository.properttiesStateFlow.collect { properties ->
            emit(
                properties.map {
                    PropertyItemViewState(
                        id = it.id,
                        type = it.type,
                        city = it.city,
                        price = it.price.toString(),
                        onPropertyClicked = {
                            currentPropertyIdRepository.setCurrentPropertyId(it.id)
                        }
                    )
                }
            )
        }
    }
}
