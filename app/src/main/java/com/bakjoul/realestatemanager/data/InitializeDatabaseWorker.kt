package com.bakjoul.realestatemanager.data

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bakjoul.realestatemanager.data.photos.PhotoDao
import com.bakjoul.realestatemanager.data.property.PropertyDao
import com.bakjoul.realestatemanager.data.property.model.PropertyDto
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import com.bakjoul.realestatemanager.data.utils.fromJson
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
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
    private val propertyDao: PropertyDao,
    private val photoDao: PhotoDao,
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
            val propertyEntities = gson.fromJson<List<PropertyDto>>(json = propertiesAsJson)
            val photosEntities = gson.fromJson<List<PhotoDto>>(json = photosAsJson)

            if (propertyEntities != null && photosEntities != null) {
                val propertyJobs = propertyEntities.map { propertyEntity ->
                    async { propertyDao.insert(propertyEntity) }
                }

                val photoJobs = photosEntities.map { photoEntity ->
                    async { photoDao.insert(photoEntity) }
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
