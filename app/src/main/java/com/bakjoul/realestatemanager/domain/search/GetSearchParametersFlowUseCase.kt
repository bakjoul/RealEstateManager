package com.bakjoul.realestatemanager.domain.search

import com.bakjoul.realestatemanager.domain.search.model.SearchParametersEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSearchParametersFlowUseCase @Inject() constructor(private val searchRepository: SearchRepository) {
    fun invoke(): Flow<SearchParametersEntity> = searchRepository.getSearchParametersFlow()
}
