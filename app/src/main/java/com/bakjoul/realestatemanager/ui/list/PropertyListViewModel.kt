package com.bakjoul.realestatemanager.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.current_property.SetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.GetPropertiesFlowUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
) : ViewModel() {

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData {
        getPropertiesFlowUseCase.invoke().collect { properties ->
            emit(
                properties.map {
                    PropertyItemViewState(
                        id = it.id,
                        photoUrl = it.photos.firstOrNull()?.url ?: "",
                        type = it.type,
                        city = it.city,
                        features = formatFeatures(it.bedrooms, it.bathrooms, it.surface),
                        price = it.price.toString(),
                        onPropertyClicked = EquatableCallback {
                            setCurrentPropertyIdUseCase.invoke(it.id)
                        }
                    )
                }
            )
        }
    }

    private fun formatFeatures(bedrooms: Int, bathrooms: Int, surface: Int): String {
        return "$bedrooms bed. - $bathrooms bath. - $surface sqm"
    }


}
