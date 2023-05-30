package com.bakjoul.realestatemanager.domain.property

import javax.inject.Inject

class SetPropertyIdUseCase @Inject constructor(private val currentPropertyRepository: CurrentPropertyRepository) {
    fun invoke(currentId: Long?) {
        currentPropertyRepository.setCurrentPropertyId(currentId)
    }
}
