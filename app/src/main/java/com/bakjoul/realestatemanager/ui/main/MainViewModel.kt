package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.GetCurrentUserUseCase
import com.bakjoul.realestatemanager.domain.auth.IsUserAuthenticatedUseCase
import com.bakjoul.realestatemanager.domain.auth.LogOutUseCase
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.drafts.AddPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GenerateNewDraftIdUseCase
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
    agentRepository: AgentRepository,
    private val isTabletUseCase: IsTabletUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val generateNewDraftIdUseCase: GenerateNewDraftIdUseCase,
    private val addPropertyDraftUseCase: AddPropertyDraftUseCase
) : ViewModel() {

    init {
        // TODO Bakjoul repenser cette partie
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
        combine(
            isTabletUseCase.invoke(),
            getCurrentNavigationUseCase.invoke()
        ) { isTablet, navigation ->
            val viewAction = when (navigation) {
                is To.Details -> if (isTablet) {
                    MainViewAction.ShowDetailsTablet
                } else {
                    MainViewAction.ShowDetailsPortrait
                }
                is To.CloseDetails -> if (isTablet) {
                    MainViewAction.CloseDetailsTablet
                } else {
                    MainViewAction.CloseDetailsPortrait
                }
                is To.PhotosDialog -> if (isTablet) {
                    MainViewAction.ShowPhotosDialogAndHideDetailsPortrait
                } else {
                    MainViewAction.ShowPhotosDialog
                }
                is To.DraftDialog -> MainViewAction.ShowPropertyDraftDialog
                is To.DraftListDialog -> MainViewAction.ShowDraftListDialog
                is To.AddProperty -> MainViewAction.ShowAddPropertyDialog(navigation.draftId, navigation.isNewDraft)
                is To.CloseAddProperty -> if (isTablet) {
                    MainViewAction.HideDetailsPortrait
                } else {
                    MainViewAction.ShowDetailsPortraitIfNeeded
                }
                is To.Dispatcher -> MainViewAction.ReturnToDispatcher
                is To.Settings -> if (isTablet) {
                    MainViewAction.ShowSettingsAndHideDetailsPortrait
                } else {
                    MainViewAction.ShowSettings
                }
                is To.CloseSettings -> if (!isTablet) {
                    MainViewAction.ShowDetailsPortraitIfNeeded
                } else {
                    null
                }
                else -> null
            }
            viewAction?.let {
                emit(Event(it))
            }
        }.collect()
    }

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }

    fun onLogOut() {
        logOutUseCase.invoke()
    }

    fun onSettingsClicked() {
        navigateUseCase.invoke(To.Settings)
    }

    fun onAddNewPropertyClicked() {
        viewModelScope.launch {
            val propertyDraftId = generateNewDraftIdUseCase.invoke()
            addPropertyDraftUseCase.invoke(PropertyFormEntity(propertyDraftId, lastUpdate = LocalDateTime.now()))
            navigateUseCase.invoke(To.AddProperty(propertyDraftId, true))
        }
    }

    fun onContinueEditingDraftClicked() {
        navigateUseCase.invoke(To.DraftListDialog)
    }
}
