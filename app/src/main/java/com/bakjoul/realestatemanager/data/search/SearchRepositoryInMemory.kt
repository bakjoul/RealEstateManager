package com.bakjoul.realestatemanager.data.search

import com.bakjoul.realestatemanager.domain.search.SearchRepository
import com.bakjoul.realestatemanager.domain.search.model.SearchParametersEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SearchRepositoryInMemory @Inject constructor() : SearchRepository {

    private val searchParametersEntityMutableStateFlow: MutableStateFlow<SearchParametersEntity> = MutableStateFlow(
        SearchParametersEntity()
    )

    override fun getSearchParametersFlow(): Flow<SearchParametersEntity> = searchParametersEntityMutableStateFlow.asStateFlow()

    override fun setSearchParameters(searchParametersEntity: SearchParametersEntity) {
        searchParametersEntityMutableStateFlow.value = searchParametersEntity
    }
}
