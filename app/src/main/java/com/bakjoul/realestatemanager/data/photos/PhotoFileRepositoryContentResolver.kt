package com.bakjoul.realestatemanager.data.photos

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.PhotoFileRepository
import com.bakjoul.realestatemanager.ui.utils.IdGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoFileRepositoryContentResolver @Inject constructor(
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val contentResolver: ContentResolver,
    @ApplicationContext private val context: Context,
    private val clock: Clock
) : PhotoFileRepository {

    private companion object {
        private const val TAG = "PhotoFileRepositoryImpl"
    }

    override suspend fun savePhotosToAppFiles(photoUris: List<String>): List<String>? =
        withContext(coroutineDispatcherProvider.io) {
            val uriList: MutableList<String> = mutableListOf()
            photoUris.forEach {
                try {
                    val inputStream = contentResolver.openInputStream(Uri.parse(it))
                    val fileName = DateTimeFormatter
                        .ofPattern(context.getString(R.string.photo_filename_format))
                        .format(LocalDateTime.now(clock))
                    val fileNameSuffix = "_${IdGenerator.generateShortUuid()}.jpg"
                    val formattedFileName = "IMG_${fileName}${fileNameSuffix}"
                    val cacheFile = File(context.cacheDir, formattedFileName)
                    val outputStream = cacheFile.outputStream()
                    inputStream?.copyTo(outputStream)
                    outputStream.close()
                    inputStream?.close()
                    uriList.add(cacheFile.absolutePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return@withContext null
                }
            }
            return@withContext uriList
        }

    override suspend fun deletePhotosFromAppFiles(photoUris: List<String>) =
        withContext(coroutineDispatcherProvider.io) {
            photoUris.forEach {
                if (File(it).exists()) {
                    File(it).delete()
                    Log.i(TAG, "Deleted cache file $it")
                } else {
                    Log.i(TAG, "Cache file $it does not exist")
                }
            }
        }
}
