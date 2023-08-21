package com.bakjoul.realestatemanager.ui.camera.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.camera.GetCameraViewActionUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CameraActivityViewModel @Inject constructor(getCameraViewActionUseCase: GetCameraViewActionUseCase) : ViewModel() {

    val viewActionLiveData: LiveData<Event<CameraActivityViewAction>> =
        getCameraViewActionUseCase.invoke()
            .map { Event(it) }
            .asLiveData()
}
