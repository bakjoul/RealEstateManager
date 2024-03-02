package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeletePhotosUseCase @Inject constructor(
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val photoRepository: PhotoRepository,
    private val photoFileRepository: PhotoFileRepository
) {
    suspend fun invoke(photoIds: List<Long>, photoUris: List<String>): Int? = withContext(coroutineDispatcherProvider.io) {
            var deletedLines: Int? = null
            buildList {
                add(async { deletedLines = photoRepository.deletePhotos(photoIds) })
                add(async { photoFileRepository.deletePhotosFromAppFiles(photoUris) })
            }.awaitAll()

            deletedLines
    }
}
