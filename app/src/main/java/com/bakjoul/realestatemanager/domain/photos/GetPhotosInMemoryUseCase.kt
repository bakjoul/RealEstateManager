package com.bakjoul.realestatemanager.domain.photos

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosInMemoryUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke(): Flow<Map<String, String>> = photoRepository.getPhotos()
}
