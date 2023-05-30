package com.bakjoul.realestatemanager.domain.property

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentPropertyIdUseCase @Inject constructor(private val currentPropertyRepository: CurrentPropertyRepository) {
    fun invoke(): Flow<Long?> = currentPropertyRepository.getCurrentPropertyId()
}
