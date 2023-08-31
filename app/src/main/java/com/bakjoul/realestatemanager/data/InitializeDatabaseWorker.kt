package com.bakjoul.realestatemanager.data

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bakjoul.realestatemanager.data.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.data.utils.fromJson
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext

@HiltWorker
class InitializeDatabaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val addPropertyUseCase: AddPropertyUseCase,
    private val gson: Gson,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_INPUT_DATA = "KEY_INPUT_DATA"
    }

    override suspend fun doWork(): Result = withContext(coroutineDispatcherProvider.io) {
        val propertiesAsJson = inputData.getString(KEY_INPUT_DATA)

        if (propertiesAsJson != null) {
            val propertyEntities = gson.fromJson<List<PropertyEntity>>(json = propertiesAsJson)

            if (propertyEntities != null) {
                propertyEntities.forEach { propertyEntity ->
                    addPropertyUseCase.invoke(propertyEntity)
                }
                Result.success()
            } else {
                Log.e(javaClass.simpleName, "Can't parse properties: $propertiesAsJson")
                Result.failure()
            }
        } else {
            Log.e(
                javaClass.simpleName,
                "Failed to get data with key $KEY_INPUT_DATA from data: $inputData"
            )
            Result.failure()
        }
    }

}
