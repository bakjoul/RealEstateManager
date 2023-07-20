package com.bakjoul.realestatemanager.domain.current_photo

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentPhotoIdAsEventUseCase @Inject constructor(private val currentPhotoIdRepository: CurrentPhotoIdRepository) {
    fun invoke(): Flow<Int> = currentPhotoIdRepository.getCurrentPhotoIdFlowAsEvent()
}
