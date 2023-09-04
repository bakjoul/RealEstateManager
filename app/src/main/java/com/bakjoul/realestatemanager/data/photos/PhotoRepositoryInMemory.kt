package com.bakjoul.realestatemanager.data.photos

import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryInMemory @Inject constructor() : PhotoRepository {

    private val currentlyAddedPhotosMap: MutableMap<String, String> = mutableMapOf()
    private val currentlyAddedPhotosMutableSharedFlow = MutableSharedFlow<Map<String, String>>(replay = 1)

    override fun addPhoto(photoUrl: String, description: String) {
        currentlyAddedPhotosMap[photoUrl] = description
        currentlyAddedPhotosMutableSharedFlow.tryEmit(currentlyAddedPhotosMap)
    }

    override fun getPhotos(): Flow<Map<String, String>> = currentlyAddedPhotosMutableSharedFlow.asSharedFlow()

    override fun clearPhotos() {
        currentlyAddedPhotosMap.clear()
        currentlyAddedPhotosMutableSharedFlow.tryEmit(currentlyAddedPhotosMap)
    }
}
