package com.bakjoul.realestatemanager.domain.main

import kotlinx.coroutines.flow.Flow

interface MainToastStateRepository {

    fun shouldShowClipboardToastFlow(): Flow<Boolean>

    fun setShouldShowClipboardToast(shouldShow: Boolean)
}
