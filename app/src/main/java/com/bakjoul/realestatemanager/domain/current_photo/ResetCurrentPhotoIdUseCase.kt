package com.bakjoul.realestatemanager.domain.current_photo

import javax.inject.Inject

class ResetCurrentPhotoIdUseCase @Inject constructor(private val currentPhotoIdRepository: CurrentPhotoIdRepository) {
    fun invoke() = currentPhotoIdRepository.resetCurrentPhotoId()
}
