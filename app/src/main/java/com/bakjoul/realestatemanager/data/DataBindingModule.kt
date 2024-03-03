package com.bakjoul.realestatemanager.data

import com.bakjoul.realestatemanager.data.agent.AgentRepositoryImplementation
import com.bakjoul.realestatemanager.data.auth.AuthRepositoryImplementation
import com.bakjoul.realestatemanager.data.autocomplete.AutocompleteRepositoryImplementation
import com.bakjoul.realestatemanager.data.currency_rate.CurrencyRateRepositoryImplementation
import com.bakjoul.realestatemanager.data.current_property.CurrentPropertyIdRepositoryImplementation
import com.bakjoul.realestatemanager.data.geocoding.GeocodingRepositoryImplementation
import com.bakjoul.realestatemanager.data.main.MainToastStateRepositoryInMemory
import com.bakjoul.realestatemanager.data.navigation.NavigationRepositoryInMemory
import com.bakjoul.realestatemanager.data.photo_preview.PhotoPreviewRepositoryImplementation
import com.bakjoul.realestatemanager.data.photos.PhotoFileRepositoryContentResolver
import com.bakjoul.realestatemanager.data.photos.PhotoRepositoryRoom
import com.bakjoul.realestatemanager.data.property.PropertyRepositoryRoom
import com.bakjoul.realestatemanager.data.resources.ResourcesRepositoryImplementation
import com.bakjoul.realestatemanager.data.settings.SettingsRepositoryImplementation
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.AuthRepository
import com.bakjoul.realestatemanager.domain.autocomplete.AutocompleteRepository
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import com.bakjoul.realestatemanager.domain.geocoding.GeocodingRepository
import com.bakjoul.realestatemanager.domain.main.MainToastStateRepository
import com.bakjoul.realestatemanager.domain.navigation.NavigationRepository
import com.bakjoul.realestatemanager.domain.photo_preview.PhotoPreviewRepository
import com.bakjoul.realestatemanager.domain.photos.PhotoFileRepository
import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
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
    abstract fun bindPropertyRepository(propertyRepositoryRoom: PropertyRepositoryRoom): PropertyRepository

    @Singleton
    @Binds
    abstract fun bindSettingsRepository(settingsRepositoryImplementation: SettingsRepositoryImplementation): SettingsRepository

    @Singleton
    @Binds
    abstract fun bindCurrencyRateRepository(currencyRateRepositoryImplementation: CurrencyRateRepositoryImplementation): CurrencyRateRepository

    @Singleton
    @Binds
    abstract fun bindAuthRepository(authRepositoryImplementation: AuthRepositoryImplementation): AuthRepository

    @Singleton
    @Binds
    abstract fun bindAgentRepository(agentRepositoryImplementation: AgentRepositoryImplementation): AgentRepository

    @Singleton
    @Binds
    abstract fun bindAutocompleteRepository(autocompleteRepositoryImplementation: AutocompleteRepositoryImplementation): AutocompleteRepository

    @Singleton
    @Binds
    abstract fun bindGeocodingRepository(geocodingRepositoryImplementation: GeocodingRepositoryImplementation): GeocodingRepository

    @Singleton
    @Binds
    abstract fun bindCameraRepository(cameraRepositoryImplementation: PhotoPreviewRepositoryImplementation): PhotoPreviewRepository

    @Singleton
    @Binds
    abstract fun bindNavigationRepository(navigationRepositoryInMemory: NavigationRepositoryInMemory): NavigationRepository

    @Singleton
    @Binds
    abstract fun bindPhotoRepository(photoRepositoryRoom: PhotoRepositoryRoom): PhotoRepository

    @Singleton
    @Binds
    abstract fun bindMainToastStateRepository(mainToastStateRepositoryInMemory: MainToastStateRepositoryInMemory): MainToastStateRepository

    @Singleton
    @Binds
    abstract fun bindPhotoFileRepository(photoFileRepositoryContentResolver: PhotoFileRepositoryContentResolver): PhotoFileRepository
}
