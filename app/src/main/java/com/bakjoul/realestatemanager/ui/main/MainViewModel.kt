package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.GetCurrentUserUseCase
import com.bakjoul.realestatemanager.domain.auth.IsUserAuthenticatedUseCase
import com.bakjoul.realestatemanager.domain.auth.LogOutUseCase
import com.bakjoul.realestatemanager.domain.main.GetClipboardToastStateUseCase
import com.bakjoul.realestatemanager.domain.main.SetClipboardToastStateUseCase
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.drafts.AddPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GenerateNewDraftIdUseCase
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val addPropertyDraftUseCase: AddPropertyDraftUseCase,
    private val getClipboardToastStateUseCase: GetClipboardToastStateUseCase,
    private val setClipboardToastStateUseCase: SetClipboardToastStateUseCase
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
                is To.Toast -> {
                    if (navigation.message == NativeText.Resource(R.string.property_address_clipboard)) {
                        val shouldShowClipboardToast = getClipboardToastStateUseCase.invoke().first()
                        if (isTablet) {
                            MainViewAction.ShowClipboardToastAndDetailsTabletIfNeeded(navigation.message, shouldShowClipboardToast)
                        } else {
                            MainViewAction.ShowClipboardToastAndDetailsPortraitIfNeeded(navigation.message, shouldShowClipboardToast)
                        }
                    } else {
                        null
                    }
                }
                is To.Photos -> if (isTablet) {
                    MainViewAction.ShowPhotosAndHideDetailsPortrait
                } else {
                    MainViewAction.ShowPhotosDialogAndDetailsPortraitIfNeeded
                }
                is To.DraftAlertDialog -> MainViewAction.ShowPropertyDraftAlertDialog
                is To.DraftList -> if (isTablet) {
                    MainViewAction.ShowDraftListAndHideDetailsPortraitIfNeeded
                } else {
                    MainViewAction.ShowDraftListAndDetailsPortraitIfNeeded
                }
                is To.AddProperty -> if (isTablet) {
                    MainViewAction.ShowAddPropertyAndHideDetailsPortraitIfNeeded(navigation.draftId, navigation.isNewDraft)
                } else {
                    MainViewAction.ShowAddPropertyAndDetailsPortraitIfNeeded(navigation.draftId, navigation.isNewDraft)
                }
                is To.ClosePhotos -> if (isTablet) {
                    MainViewAction.ShowDetailsTablet
                } else {
                    MainViewAction.ShowDetailsPortraitIfNeeded
                }
                is To.CloseDraftList, To.CloseAddProperty, To.CloseSettings, To.CloseLoanSimulator -> if (isTablet) {
                    MainViewAction.HideDetailsPortrait
                } else {
                    MainViewAction.ShowDetailsPortraitIfNeeded
                }
                is To.Dispatcher -> MainViewAction.ReturnToDispatcher
                is To.Settings -> if (isTablet) {
                    MainViewAction.ShowSettingsAndHideDetailsPortraitIfNeeded
                } else {
                    MainViewAction.ShowSettingsAndDetailsPortraitIfNeeded
                }
                is To.LoanSimulator -> if (isTablet) {
                    MainViewAction.ShowLoanSimulatorAndHideDetailsPortraitIfNeeded
                } else {
                    MainViewAction.ShowLoanSimulatorAndDetailsPortraitIfNeeded
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
        navigateUseCase.invoke(To.DraftList)
    }

    fun onLoanSimulatorClicked() {
        navigateUseCase.invoke(To.LoanSimulator)
    }

    fun onClipboardToastShown() {
        setClipboardToastStateUseCase.invoke(false)
    }

    fun onDraftAlertDialogDismissed() {
        navigateUseCase.invoke(To.DoNothing)
    }
}
