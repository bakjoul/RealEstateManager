package com.bakjoul.realestatemanager.domain.current_property

import javax.inject.Inject

class SetCurrentPropertyIdUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
    fun invoke(currentId: Long) {
        currentPropertyIdRepository.setCurrentPropertyId(currentId)
    }
}
