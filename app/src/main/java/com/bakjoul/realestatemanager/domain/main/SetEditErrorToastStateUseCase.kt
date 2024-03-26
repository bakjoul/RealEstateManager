package com.bakjoul.realestatemanager.domain.main

import javax.inject.Inject

class SetEditErrorToastStateUseCase @Inject constructor(private val mainToastStateRepository: MainToastStateRepository) {
    fun invoke(shouldShow: Boolean) {
        mainToastStateRepository.setShouldShowEditErrorToast(shouldShow)
    }
}
