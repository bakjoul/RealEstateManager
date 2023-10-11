package com.bakjoul.realestatemanager.domain.photos.drafts

import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
import javax.inject.Inject

class DeletePhotoDraftUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(id: Long) {
        photoRepository.deletePhotoDraft(id)
    }
}
