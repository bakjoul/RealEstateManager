package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.ui.main.MainViewAction
import javax.inject.Inject

class SetAddPropertyViewActionUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    fun invoke(action: MainViewAction) {
        propertyRepository.setAddPropertyViewAction(action)
    }
}
