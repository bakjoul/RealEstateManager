package com.bakjoul.realestatemanager.domain.photos.edit

import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.content_resolver.PhotoFileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeletePhotosForExistingPropertyDraftUseCase @Inject constructor(
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val temporaryPhotoRepository: TemporaryPhotoRepository,
    private val photoFileRepository: PhotoFileRepository
){
    suspend fun invoke(photoIds: List<Long>, photoUris: List<String>): Int? = withContext(coroutineDispatcherProvider.io) {
        var deletedLines: Int? = null
        buildList {
            add(async { deletedLines = temporaryPhotoRepository.deletePhotosForExistingPropertyDraft(photoIds) })
            add(async { photoFileRepository.deletePhotosFromAppFiles(photoUris) })
        }.awaitAll()

        deletedLines
    }
}
