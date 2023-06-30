package com.bakjoul.realestatemanager.domain.current_property

import javax.inject.Inject

class ResetCurrentPropertyIdUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
    fun invoke() = currentPropertyIdRepository.resetCurrentPropertyId()
}
