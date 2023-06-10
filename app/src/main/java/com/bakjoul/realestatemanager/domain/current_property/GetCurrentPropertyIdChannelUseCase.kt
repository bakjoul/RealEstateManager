package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

class GetCurrentPropertyIdChannelUseCase @Inject constructor(private val currentPropertyIdRepository: CurrentPropertyIdRepository) {
    fun invoke(): Channel<Long> = currentPropertyIdRepository.getCurrentPropertyIdChannel()
}
