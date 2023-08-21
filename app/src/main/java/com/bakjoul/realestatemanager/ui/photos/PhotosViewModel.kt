package com.bakjoul.realestatemanager.ui.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_photo.GetPhotosDialogViewActionUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetPhotosDialogViewActionUseCase
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.ui.details.DetailsMediaItemViewState
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val setCurrentPhotoIdUseCase: SetCurrentPhotoIdUseCase,
    private val setPhotosDialogViewActionUseCase: SetPhotosDialogViewActionUseCase,
    getPhotosDialogViewActionUseCase: GetPhotosDialogViewActionUseCase,
    getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    getCurrentPhotoIdUseCase: GetCurrentPhotoIdUseCase
    ) : ViewModel() {

    val viewStateLiveData: LiveData<PhotosViewState> = liveData {
        combine(
            getCurrentPropertyUseCase.invoke(),
            getCurrentPhotoIdUseCase.invoke()
        ) { property, currentPhotoId ->
            PhotosViewState(
                photosUrls = property.photos.map { it.url },
                thumbnails = mapPhotosToMediaItemViewStates(property.photos),
                currentPhotoId = currentPhotoId
            )
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<PhotosDialogViewAction>> =
        getPhotosDialogViewActionUseCase.invoke().map {
            Event(it)
        }.asLiveData()

    private fun mapPhotosToMediaItemViewStates(photoEntities: List<PhotoEntity>): List<DetailsMediaItemViewState> {
        return photoEntities.map { photoEntity ->
            DetailsMediaItemViewState(
                id = photoEntity.id,
                url = photoEntity.url,
                description = photoEntity.description,
                onPhotoClicked = EquatableCallback { setCurrentPhotoIdUseCase.invoke(photoEntity.id.toInt()) }
            )
        }
    }

    fun onCloseButtonClicked() {
        setPhotosDialogViewActionUseCase.invoke(PhotosDialogViewAction.ClosePhotosDialog)
    }

    fun updateCurrentPhotoId(position: Int) = setCurrentPhotoIdUseCase.invoke(position)
}
