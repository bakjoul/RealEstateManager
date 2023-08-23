package com.bakjoul.realestatemanager.domain.current_property

import javax.inject.Inject

class GetDetailsViewActionUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
    fun invoke() = currentPropertyIdRepository.getDetailsViewActionFlow()
}
