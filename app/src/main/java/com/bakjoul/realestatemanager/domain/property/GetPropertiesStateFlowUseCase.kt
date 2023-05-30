package com.bakjoul.realestatemanager.domain.property

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPropertiesStateFlowUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    fun invoke() = propertyRepository.getPropertiesStateFlow()
}
