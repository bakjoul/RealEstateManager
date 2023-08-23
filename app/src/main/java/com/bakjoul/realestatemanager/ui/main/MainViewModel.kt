package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.GetCurrentUserUseCase
import com.bakjoul.realestatemanager.domain.auth.IsUserAuthenticatedUseCase
import com.bakjoul.realestatemanager.domain.auth.LogOutUseCase
import com.bakjoul.realestatemanager.domain.current_photo.GetPhotosDialogViewActionUseCase
import com.bakjoul.realestatemanager.domain.current_property.GetDetailsViewActionUseCase
import com.bakjoul.realestatemanager.domain.property.GetAddPropertyViewActionUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.photos.PhotosDialogViewAction
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getDetailsViewActionUseCase: GetDetailsViewActionUseCase,
    isTabletUseCase: IsTabletUseCase,
    getPhotosDialogViewActionUseCase: GetPhotosDialogViewActionUseCase,
    isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
    agentRepository: AgentRepository,
    getAddPropertyViewActionUseCase: GetAddPropertyViewActionUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val logOutUseCase: LogOutUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            isUserAuthenticatedUseCase.invoke().collect { isAuthenticated ->
                if (isAuthenticated) {
                    val currentUser = getCurrentUserUseCase.invoke()
                    agentRepository.createFirestoreAgent(currentUser)
                }
            }
        }
    }

    val mainViewActionLiveData: LiveData<Event<MainViewAction>> = liveData {
        coroutineScope {
            launch {
                combine(
                    isTabletUseCase.invoke(),
                    getDetailsViewActionUseCase.invoke()
                ) { isTablet, viewAction ->
                    if (isTablet) {
                        if (viewAction is MainViewAction.ShowTabletDetails || viewAction is MainViewAction.ClearDetailsTablet) {
                            emit(Event(viewAction))
                        } else if (viewAction is MainViewAction.ShowPortraitDetails) {
                            emit(Event(MainViewAction.ShowTabletDetails))
                        }
                    } else {
                        if (viewAction is MainViewAction.ShowPortraitDetails) {
                            emit(Event(viewAction))
                        } else if (viewAction is MainViewAction.ShowTabletDetails) {
                            emit(Event(MainViewAction.ShowPortraitDetails))
                        }
                    }
                }.collect()
            }

            launch {
                combine(
                    getPhotosDialogViewActionUseCase.invoke(),
                    isTabletUseCase.invoke()
                ) { viewAction, isTablet ->
                    if (isTablet) {
                        if (viewAction is PhotosDialogViewAction.ShowPhotosDialog) {
                            emit(Event(MainViewAction.ShowPhotosDialog))
                        }
                    }
                }.collect()
            }

            launch {
                getAddPropertyViewActionUseCase.invoke().collect {
                    if (it is MainViewAction.ShowAddPropertyDialog) {
                        emit(Event(it))
                    }
                }
            }
        }
    }

    fun onResume(isTablet: Boolean) = refreshOrientationUseCase.invoke(isTablet)

    fun logOut() = logOutUseCase.invoke()
}
