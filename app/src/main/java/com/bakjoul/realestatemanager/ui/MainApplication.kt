package com.bakjoul.realestatemanager.ui

import android.app.Application
import com.bakjoul.realestatemanager.domain.currency_rate.UpdateEuroRateUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var updateEuroRateUseCase: UpdateEuroRateUseCase

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.Default).launch {
            updateEuroRateUseCase.invoke()
        }
    }
}
