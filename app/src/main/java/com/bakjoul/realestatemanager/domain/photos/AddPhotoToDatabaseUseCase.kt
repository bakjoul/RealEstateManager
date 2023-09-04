package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.data.photos.PhotoDao
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import javax.inject.Inject

class AddPhotoToDatabaseUseCase @Inject constructor(private val photoDao: PhotoDao) {
    suspend fun invoke(photo: PhotoEntity) = photoDao.insert(photo)
}
