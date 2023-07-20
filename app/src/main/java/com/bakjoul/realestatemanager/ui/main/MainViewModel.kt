package com.bakjoul.realestatemanager.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.GetCurrentUserUseCase
import com.bakjoul.realestatemanager.domain.auth.IsUserAuthenticatedUseCase
import com.bakjoul.realestatemanager.domain.auth.LogOutUseCase
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdAsEventUseCase
import com.bakjoul.realestatemanager.domain.current_property.GetCurrentPropertyIdAsEventUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getCurrentPropertyIdAsEventUseCase: GetCurrentPropertyIdAsEventUseCase,
    isTabletUseCase: IsTabletUseCase,
    getCurrentPhotoIdAsEventUseCase: GetCurrentPhotoIdAsEventUseCase,
    isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
    agentRepository: AgentRepository,
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
                    getCurrentPropertyIdAsEventUseCase.invoke(),
                    isTabletUseCase.invoke()
                ) { _, isTablet ->
                    if (!isTablet) {
                        Log.d("test", "navigatetodetails: ")
                        emit(Event(MainViewAction.NavigateToDetails))
                    }
                }.collect()
            }

            launch {
                getCurrentPhotoIdAsEventUseCase.invoke().collect {
                    emit(Event(MainViewAction.DisplayPhotosDialog))
                }
            }
        }
    }

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }

    fun logOut() = logOutUseCase.invoke()
}
