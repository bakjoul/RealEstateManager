package com.bakjoul.realestatemanager.domain.search

import com.bakjoul.realestatemanager.domain.search.model.SearchParametersEntity
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun getSearchParametersFlow(): Flow<SearchParametersEntity>

    fun setSearchParameters(searchParametersEntity: SearchParametersEntity)
}
