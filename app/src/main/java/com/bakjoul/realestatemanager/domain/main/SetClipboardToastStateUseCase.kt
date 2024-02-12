package com.bakjoul.realestatemanager.domain.main

import javax.inject.Inject

class SetClipboardToastStateUseCase @Inject constructor(private val mainToastStateRepository: MainToastStateRepository) {
    fun invoke(shouldShow: Boolean) {
        mainToastStateRepository.setShouldShowClipboardToast(shouldShow)
    }
}
