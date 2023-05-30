package com.bakjoul.realestatemanager.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.GetPropertiesStateFlowUseCase
import com.bakjoul.realestatemanager.domain.property.SetPropertyIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val getPropertiesStateFlowUseCase: GetPropertiesStateFlowUseCase,
    private val setPropertyIdUseCase: SetPropertyIdUseCase,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData(coroutineDispatcherProvider.io) {
            getPropertiesStateFlowUseCase.invoke().collect { properties ->
                emit(
                    properties.map {
                        PropertyItemViewState(
                            id = it.id,
                            type = it.type,
                            city = it.city,
                            price = it.price.toString(),
                            onPropertyClicked = {
                                setPropertyIdUseCase.invoke(it.id)
                            }
                        )
                    }
                )
            }
        }
}
