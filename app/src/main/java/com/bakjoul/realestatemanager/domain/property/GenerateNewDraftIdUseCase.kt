package com.bakjoul.realestatemanager.domain.property

import javax.inject.Inject

class GenerateNewDraftIdUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(): Long {
        return propertyRepository.generateNewDraftId()
    }
}
