package com.bakjoul.realestatemanager.ui.camera.photo_preview

import android.text.Editable
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.camera.DeleteCapturedPhotoUseCase
import com.bakjoul.realestatemanager.domain.camera.GetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.AddPhotoUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoPreviewViewModel @Inject constructor(
    private val getCapturedPhotoUriUseCase: GetCapturedPhotoUriUseCase,
    private val deleteCapturedPhotoUseCase: DeleteCapturedPhotoUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val addPhotoUseCase: AddPhotoUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase
) : ViewModel() {

    private val descriptionMutableStateFlow = MutableStateFlow<String?>(null)
    private val isDoneButtonClickedMutableStateFlow = MutableStateFlow(false)
    private var photoUri: String? = null

    val viewStateLiveData: LiveData<PhotoPreviewViewState> = liveData {
        combine(
            getCapturedPhotoUriUseCase.invoke(),
            descriptionMutableStateFlow.asStateFlow(),
            isDoneButtonClickedMutableStateFlow.asStateFlow()
        ) { photo, description, isDoneButtonClicked ->
            photoUri = photo
            val descriptionError = if (isDoneButtonClicked && description.isNullOrEmpty()) {
                NativeText.Resource(R.string.photo_preview_description_error)
            } else {
                null
            }
            PhotoPreviewViewState(photo, descriptionError)
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<PhotoPreviewViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.Toast -> emit(Event(PhotoPreviewViewAction.ShowToast(it.message)))
                else -> Unit
            }
        }
    }

    fun onCancelButtonClicked() {
        photoUri?.let {
            deleteCapturedPhotoUseCase.invoke(photoUri!!.toUri())
        }
        navigateUseCase.invoke(To.ClosePhotoPreview)
    }

    fun onDoneButtonClicked() {
        isDoneButtonClickedMutableStateFlow.value = true
        if (!descriptionMutableStateFlow.value.isNullOrEmpty()) {
            photoUri?.let {
                viewModelScope.launch {
                    val photoId = addPhotoUseCase.invoke(savedStateHandle.get<Long>("propertyId")!!, it, descriptionMutableStateFlow.value!!)
                    if (photoId != null) {
                        navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.photo_preview_added_toast)))
                    }
                }
            }
            navigateUseCase.invoke(To.CloseCamera)
        }
    }

    fun onDescriptionChanged(description: Editable?) {
        isDoneButtonClickedMutableStateFlow.value = false
        if (description != null) {
            descriptionMutableStateFlow.value = description.toString()
        }
    }
}
