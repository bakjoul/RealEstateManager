package com.bakjoul.realestatemanager.ui.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.ui.details.DetailsMediaItemViewState
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    getCurrentPropertyUseCase: GetCurrentPropertyUseCase,
    getCurrentPhotoIdUseCase: GetCurrentPhotoIdUseCase,
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val setCurrentPhotoIdUseCase: SetCurrentPhotoIdUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    val viewStateLiveData: LiveData<PhotosViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(
            getCurrentPropertyUseCase.invoke(),
            getCurrentPhotoIdUseCase.invoke()
        ) { property, currentPhotoId ->
            PhotosViewState(
                photosUrls = emptyList(),//property.photos.map { it.url },
                thumbnails = emptyList(),//mapPhotosToMediaItemViewStates(property.photos),
                currentPhotoId = currentPhotoId
            )
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<PhotosViewAction>> =
        getCurrentNavigationUseCase.invoke()
            .mapNotNull {
                when (it) {
                    is To.ClosePhotosDialog -> Event(PhotosViewAction.CloseDialog)
                    else -> null
                }
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
        navigateUseCase.invoke(To.ClosePhotosDialog)
    }

    fun updateCurrentPhotoId(position: Int) {
        setCurrentPhotoIdUseCase.invoke(position)
    }
}
