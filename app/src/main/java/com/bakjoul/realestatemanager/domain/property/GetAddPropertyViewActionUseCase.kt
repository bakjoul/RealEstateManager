package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.ui.main.MainViewAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAddPropertyViewActionUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    fun invoke(): Flow<MainViewAction> = propertyRepository.getAddPropertyViewActionFlow()
}
