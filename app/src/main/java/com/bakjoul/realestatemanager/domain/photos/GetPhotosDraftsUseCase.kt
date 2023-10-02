package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class GetPhotosDraftsUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke() = photoRepository.getPhotosDrafts()
}
