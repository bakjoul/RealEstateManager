package com.bakjoul.realestatemanager.ui.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListMapper
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.SelectType
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.GetPhotosForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.photos.edit.GetPhotosForExistingPropertyDraftIdUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    getPhotosForPropertyIdUseCase: GetPhotosForPropertyIdUseCase,
    getPhotosForExistingPropertyDraftUseCase: GetPhotosForExistingPropertyDraftIdUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val propertyId = requireNotNull(savedStateHandle.get<Long>("propertyId")) {
        "No property id passed as parameter !"
    }
    private val isExistingProperty = savedStateHandle.get<Boolean>("isExistingProperty") ?: false

    private val currentPhotoIdMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(savedStateHandle["clickedPhotoIndex"] ?: 0)
    private val photosFlow = if (isExistingProperty) {
        getPhotosForExistingPropertyDraftUseCase.invoke(propertyId)
    } else {
        getPhotosForPropertyIdUseCase.invoke(propertyId)
    }

    val viewStateLiveData: LiveData<PhotosViewState> = liveData {
        combine(
            photosFlow,
            currentPhotoIdMutableStateFlow
        ) { photos, currentPhotoId ->
            PhotosViewState(
                photosUrls = photos.map { it.uri },
                thumbnails = PhotoListMapper().map(
                    photos,
                    { if (it == currentPhotoId) SelectType.SELECTED else SelectType.NOT_SELECTED },
                    null,
                    { clickedPhotoIndex -> currentPhotoIdMutableStateFlow.value = clickedPhotoIndex }
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
                    is To.ClosePhotos, To.CloseDraftPhotos -> Event(PhotosViewAction.CloseDialog)
                    else -> null
                }
            }.asLiveData()

    fun onCloseButtonClicked() {
        if (savedStateHandle.get<Boolean>("isDraft") == true) {
            navigateUseCase.invoke(To.CloseDraftPhotos)
        } else {
            navigateUseCase.invoke(To.ClosePhotos)
        }
    }

    fun updateCurrentPhotoId(position: Int) {
        currentPhotoIdMutableStateFlow.tryEmit(position)
    }
}
