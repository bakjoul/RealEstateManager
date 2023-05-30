package com.bakjoul.realestatemanager.domain.resources

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsTabletUseCase @Inject constructor(private val resourcesRepository: ResourcesRepository) {
    fun invoke(): Flow<Boolean> = resourcesRepository.isTabletFlow()
}
