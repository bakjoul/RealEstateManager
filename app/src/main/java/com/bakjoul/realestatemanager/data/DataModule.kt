package com.bakjoul.realestatemanager.data

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    companion object {
        val PREFERENCES_FILENAME = "com.bakjoul.realestatemanager_settings"
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE)
}
