package com.bakjoul.realestatemanager.domain.resources

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshOrientationUseCase @Inject constructor(private val resourcesRepository: ResourcesRepository) {
    fun invoke() {
        resourcesRepository.setOrientation()
    }
}
