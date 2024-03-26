package com.bakjoul.realestatemanager.ui.edit_description

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.UpdatePhotoDescriptionUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPhotoDescriptionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val updatePhotoDescriptionUseCase: UpdatePhotoDescriptionUseCase
) : ViewModel() {

    private val isExistingProperty = savedStateHandle.get<Boolean>("isExistingProperty") ?: false
    private val descriptionMutableStateFlow: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get<String>("description") ?: "")
    private val isSaveButtonClickedMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val viewStateLiveData: LiveData<EditPhotoDescriptionViewState> = liveData {
        combine(
            descriptionMutableStateFlow,
            isSaveButtonClickedMutableStateFlow
        ) { description, isSaveButtonClicked ->
            val descriptionError = if (description.isEmpty() && isSaveButtonClicked) {
                NativeText.Resource(R.string.edit_photo_description_error)
            } else {
                null
            }
            EditPhotoDescriptionViewState(descriptionError)
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<EditPhotoDescriptionViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.CloseEditPhotoDescription -> emit(Event(EditPhotoDescriptionViewAction.CloseDialog))
                else -> Unit
            }
        }
    }

    fun onDescriptionChanged(description: Editable?) {
        isSaveButtonClickedMutableStateFlow.value = false
        descriptionMutableStateFlow.value = description.toString()
    }

    fun onCancelButtonClicked() {
        navigateUseCase.invoke(To.CloseEditPhotoDescription)
    }

    fun onSaveButtonClicked() {
        isSaveButtonClickedMutableStateFlow.value = true
        if (descriptionMutableStateFlow.value.isNotEmpty()) {
            viewModelScope.launch {
                updatePhotoDescriptionUseCase.invoke(
                    savedStateHandle.get<Long>("photoId")!!,
                    descriptionMutableStateFlow.value,
                    isExistingProperty
                )
            }
            navigateUseCase.invoke(To.CloseEditPhotoDescription)
        }
    }
}
