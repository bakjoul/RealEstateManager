package com.bakjoul.realestatemanager.domain.search

import com.bakjoul.realestatemanager.domain.search.model.SearchParametersEntity
import javax.inject.Inject

class SetSearchParametersUseCase @Inject constructor(private val searchRepository: SearchRepository) {
    fun invoke(searchParametersEntity: SearchParametersEntity) {
        searchRepository.setSearchParameters(searchParametersEntity)
    }
}
