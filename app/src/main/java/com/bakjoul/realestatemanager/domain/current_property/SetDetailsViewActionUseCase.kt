package com.bakjoul.realestatemanager.domain.current_property

import com.bakjoul.realestatemanager.ui.main.MainViewAction
import javax.inject.Inject

class SetDetailsViewActionUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
    fun invoke(viewAction: MainViewAction) = currentPropertyIdRepository.setDetailsViewAction(viewAction)
}
