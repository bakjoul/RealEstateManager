package com.bakjoul.realestatemanager.domain.current_photo

import javax.inject.Inject

class SetCurrentPhotoIdUseCase @Inject constructor(private val currentPhotoIdRepository: CurrentPhotoIdRepository) {
    fun invoke(currentId: Int) = currentPhotoIdRepository.setCurrentPhotoId(currentId)
}
