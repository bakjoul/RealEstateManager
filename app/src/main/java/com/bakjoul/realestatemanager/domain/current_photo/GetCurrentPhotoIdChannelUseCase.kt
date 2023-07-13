package com.bakjoul.realestatemanager.domain.current_photo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class GetCurrentPhotoIdChannelUseCase @Inject constructor(private val currentPhotoIdRepository: CurrentPhotoIdRepository) {
    fun invoke(): Flow<Int> = currentPhotoIdRepository.getCurrentPhotoIdChannel().receiveAsFlow()
}
