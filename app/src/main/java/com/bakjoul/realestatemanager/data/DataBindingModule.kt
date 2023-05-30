package com.bakjoul.realestatemanager.data

import com.bakjoul.realestatemanager.data.property.CurrentPropertyRepositoryImplementation
import com.bakjoul.realestatemanager.data.property.PropertyRepositoryImplementation
import com.bakjoul.realestatemanager.domain.ResourcesRepository
import com.bakjoul.realestatemanager.domain.property.CurrentPropertyRepository
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingModule {

    @Singleton
    @Binds
    abstract fun bindResourcesRepository(resourcesRepositoryImplementation: ResourcesRepositoryImplementation): ResourcesRepository

    @Singleton
    @Binds
    abstract fun bindCurrentPropertyRepository(currentPropertyRepositoryImplementation: CurrentPropertyRepositoryImplementation): CurrentPropertyRepository

    @Singleton
    @Binds
    abstract fun bindPropertyRepository(propertyRepositoryImplementation: PropertyRepositoryImplementation): PropertyRepository
}
