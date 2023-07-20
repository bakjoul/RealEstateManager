package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentPropertyIdAsEventUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
    fun invoke(): Flow<Long> = currentPropertyIdRepository.getCurrentPropertyIdFlowAsEvent()
}
