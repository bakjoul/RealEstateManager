package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.DeletePhotosUseCase
import com.bakjoul.realestatemanager.domain.photos.edit.DeletePhotosForExistingPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeletePropertyDraftWithPhotosUseCase @Inject constructor(
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val propertyRepository: PropertyRepository,
    private val deletePhotosForExistingPropertyDraftUseCase: DeletePhotosForExistingPropertyDraftUseCase,
    private val deletePhotosUseCase: DeletePhotosUseCase
) {
    suspend fun invoke(propertyId: Long, photosList: List<PhotoEntity>, isExistingProperty: Boolean): Boolean = withContext(coroutineDispatcherProvider.io) {
        buildList {
            add(async { propertyRepository.deletePropertyDraft(propertyId) })
            if (photosList.isNotEmpty()) {
                if (isExistingProperty) {
                    add(async { deletePhotosForExistingPropertyDraftUseCase.invoke(photosList.map { it.id }, photosList.map { it.uri }) })
                } else {
                    add(async { deletePhotosUseCase.invoke(photosList.map { it.id }, photosList.map { it.uri }) })
                }
            }
        }.awaitAll().all { it != null }
    }
}
