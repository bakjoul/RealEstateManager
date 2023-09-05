package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.current_property.GetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GetCurrentPropertyUseCase @Inject constructor(
    private val getPropertyByIdUseCase: GetPropertyByIdUseCase,
    private val getCurrentPropertyIdUseCase: GetCurrentPropertyIdUseCase,
) {
    fun invoke(): Flow<PropertyEntity> = getCurrentPropertyIdUseCase.invoke().mapLatest {
        getPropertyByIdUseCase.invoke(it)
    }.filterNotNull()
}
