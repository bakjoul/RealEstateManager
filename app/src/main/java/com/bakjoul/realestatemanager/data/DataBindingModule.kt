package com.bakjoul.realestatemanager.data

import com.bakjoul.realestatemanager.data.currency_rate.CurrencyRateRepositoryImplementation
import com.bakjoul.realestatemanager.data.current_property.CurrentPropertyIdRepositoryImplementation
import com.bakjoul.realestatemanager.data.property.PropertyRepositoryImplementation
import com.bakjoul.realestatemanager.data.resources.ResourcesRepositoryImplementation
import com.bakjoul.realestatemanager.data.settings.SettingsRepositoryImplementation
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.resources.ResourcesRepository
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
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
    abstract fun bindCurrentPropertyIdRepository(currentPropertyIdRepositoryImplementation: CurrentPropertyIdRepositoryImplementation): CurrentPropertyIdRepository

    @Singleton
    @Binds
    abstract fun bindPropertyRepository(propertyRepositoryImplementation: PropertyRepositoryImplementation): PropertyRepository

    @Singleton
    @Binds
    abstract fun bindSettingsRepository(settingsRepositoryImplementation: SettingsRepositoryImplementation): SettingsRepository

    @Singleton
    @Binds
    abstract fun bindCurrencyRateRepository(currencyRateRepositoryImplementation: CurrencyRateRepositoryImplementation): CurrencyRateRepository
}
