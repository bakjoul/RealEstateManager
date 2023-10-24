package com.bakjoul.realestatemanager.ui.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListMapper
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.SelectType
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_photo.SetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyUseCase
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
                photosUrls = property.photos.map { it.url },
                thumbnails = PhotoListMapper().map(
                    property.photos,
                    { index -> if (index == currentPhotoId) SelectType.SELECTED else SelectType.NOT_SELECTED },
                    { setCurrentPhotoIdUseCase.invoke(it) }
                ),
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

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.ClosePhotosDialog)
    }

    fun updateCurrentPhotoId(position: Int) {
        setCurrentPhotoIdUseCase.invoke(position)
    }
}
