package com.bakjoul.realestatemanager.data.main

import com.bakjoul.realestatemanager.domain.main.MainToastStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class MainToastStateRepositoryInMemory @Inject constructor() : MainToastStateRepository {

    private val shouldShowClipboardToastMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val shouldShowEditErrorToastMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override fun shouldShowClipboardToastFlow(): Flow<Boolean> = shouldShowClipboardToastMutableStateFlow

    override fun setShouldShowClipboardToast(shouldShow: Boolean) {
        shouldShowClipboardToastMutableStateFlow.value = shouldShow
    }

    override fun shouldShowEditErrorToastFlow(): Flow<Boolean> = shouldShowEditErrorToastMutableStateFlow

    override fun setShouldShowEditErrorToast(shouldShow: Boolean) {
        shouldShowEditErrorToastMutableStateFlow.value = shouldShow
    }
}
