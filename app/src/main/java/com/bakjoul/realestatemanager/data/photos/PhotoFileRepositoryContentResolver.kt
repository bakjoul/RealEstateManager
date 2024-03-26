package com.bakjoul.realestatemanager.data.photos

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.content_resolver.PhotoFileRepository
import com.bakjoul.realestatemanager.ui.utils.IdGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

    override suspend fun savePhotosToAppFiles(photoUris: List<String>, areTemporaryPhotos: Boolean): List<String>? =
        withContext(coroutineDispatcherProvider.io) {
            val uriList: MutableList<String> = mutableListOf()

            photoUris.forEach {
                val uri = Uri.parse(it)
                val isContentResolverNeeded = isResourceUri(uri) || isMediaUri(uri)

                try {
                    val inputStream = if (isContentResolverNeeded) {
                        contentResolver.openInputStream(Uri.parse(it))
                    } else {
                        FileInputStream(File(uri.path!!))
                    }

                    val fileName = generateFileName()
                    val cacheFile: File = if (areTemporaryPhotos) {
                        val subDir = File(context.cacheDir, "temp")
                        if (!subDir.exists()) {
                            subDir.mkdirs()
                        }
                        File(subDir, fileName)
                    } else {
                        File(context.cacheDir, fileName)
                    }

                    val outputStream = if (isContentResolverNeeded) {
                        cacheFile.outputStream()
                    } else {
                        FileOutputStream(cacheFile)
                    }
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

    override suspend fun moveTemporaryPhotosToMainDirectory(photoUris: List<String>): List<String>? = withContext(coroutineDispatcherProvider.io) {
        val newUriList: MutableList<String> = mutableListOf()

        photoUris.forEach {
            try {
                val file = File(Uri.parse(it).path!!)
                val fileName = file.name
                if (file.exists()) {
                    val success = file.renameTo(File(context.cacheDir, fileName))
                    if (!success) {
                        Log.e(TAG, "Error moving temporary photo to main directory")
                        return@withContext null
                    } else {
                        newUriList.add(File(context.cacheDir, fileName).absolutePath)
                    }
                } else {
                    Log.e(TAG, "Error moving temporary photo to main directory: file does not exist")
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
        return@withContext newUriList
    }

    private fun isResourceUri(uri: Uri): Boolean {
        return uri.scheme == "android.resource" && uri.authority == "com.bakjoul.realestatemanager"
    }

    private fun isMediaUri(uri: Uri): Boolean {
        return uri.scheme == "content" && uri.authority == "media"
    }

    private fun generateFileName(): String {
        val fileName = DateTimeFormatter
            .ofPattern(context.getString(R.string.photo_filename_format))
            .format(LocalDateTime.now(clock))
        val fileNameSuffix = "_${IdGenerator.generateShortUuid()}.jpg"
        return "IMG_${fileName}${fileNameSuffix}"
    }
}
