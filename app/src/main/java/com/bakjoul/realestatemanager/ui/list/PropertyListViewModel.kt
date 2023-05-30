package com.bakjoul.realestatemanager.ui.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.property.CurrentPropertyRepository
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val currentPropertyRepository: CurrentPropertyRepository
) : ViewModel() {

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData(Dispatchers.IO) {
        propertyRepository.getPropertiesStateFlow().collect { properties ->
            emit(
                properties.map {
                    PropertyItemViewState(
                        id = it.id,
                        type = it.type,
                        city = it.city,
                        price = it.price.toString(),
                        onPropertyClicked = {
                            Log.d("test", "onPropertyClicked")
                            currentPropertyRepository.setCurrentPropertyId(it.id)
                        }
                    )
                }
            )
        }
    }
}
