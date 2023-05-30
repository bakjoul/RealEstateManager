package com.bakjoul.realestatemanager.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.property.GetPropertiesFlowUseCase
import com.bakjoul.realestatemanager.domain.property.SetPropertyIdUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setPropertyIdUseCase: SetPropertyIdUseCase,
) : ViewModel() {

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData {
            getPropertiesFlowUseCase.invoke().collect { properties ->
                emit(
                    properties.map {
                        PropertyItemViewState(
                            id = it.id,
                            type = it.type,
                            city = it.city,
                            price = it.price.toString(),
                            onPropertyClicked = EquatableCallback {
                                setPropertyIdUseCase.invoke(it.id)
                            }
                        )
                    }
                )
            }
        }
}
