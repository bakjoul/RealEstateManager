package com.bakjoul.realestatemanager.domain.resources

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsTabletUseCase @Inject constructor(private val resourcesRepository: ResourcesRepository) {
    fun invoke(): Flow<Boolean> = resourcesRepository.isTabletFlow()
}
