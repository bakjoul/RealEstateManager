package com.bakjoul.realestatemanager.domain.main

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEditErrorToastStateUseCase @Inject constructor(private val mainToastStateRepository: MainToastStateRepository) {
    fun invoke(): Flow<Boolean> = mainToastStateRepository.shouldShowEditErrorToastFlow()
}
