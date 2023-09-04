package com.bakjoul.realestatemanager.data

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bakjoul.realestatemanager.domain.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.data.utils.fromJson
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.AddPhotoToDatabaseUseCase
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@HiltWorker
class InitializeDatabaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val addPropertyUseCase: AddPropertyUseCase,
    private val addPhotoToDatabaseUseCase: AddPhotoToDatabaseUseCase,
    private val gson: Gson,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_INPUT_DATA_PROPERTIES = "KEY_INPUT_DATA_PROPERTIES"
        const val KEY_INPUT_DATA_PHOTOS = "KEY_INPUT_DATA_PHOTOS"
    }

    override suspend fun doWork(): Result = withContext(coroutineDispatcherProvider.io) {
        val propertiesAsJson = inputData.getString(KEY_INPUT_DATA_PROPERTIES)
        val photosAsJson = inputData.getString(KEY_INPUT_DATA_PHOTOS)

        if (propertiesAsJson != null && photosAsJson != null) {
            val propertyEntities = gson.fromJson<List<PropertyEntity>>(json = propertiesAsJson)
            val photosEntities = gson.fromJson<List<PhotoEntity>>(json = photosAsJson)

            if (propertyEntities != null && photosEntities != null) {
                val propertyJobs = propertyEntities.map { propertyEntity ->
                    async { addPropertyUseCase.invoke(propertyEntity) }
                }

                val photoJobs = photosEntities.map { photoEntity ->
                    async { addPhotoToDatabaseUseCase.invoke(photoEntity) }
                }

                val jobs = propertyJobs + photoJobs
                jobs.awaitAll()
                Result.success()
            } else {
                Log.e(javaClass.simpleName, "Can't parse properties: $propertiesAsJson")
                Result.failure()
            }
        } else {
            Log.e(
                javaClass.simpleName,
                "Failed to get data with key $KEY_INPUT_DATA_PROPERTIES from data: $inputData"
            )
            Result.failure()
        }
    }
}
