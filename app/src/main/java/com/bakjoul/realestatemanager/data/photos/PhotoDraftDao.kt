package com.bakjoul.realestatemanager.data.photos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bakjoul.realestatemanager.data.photos.model.PhotoDraftDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDraftDao {

    @Insert
    suspend fun insert(photo: PhotoDraftDto)

    @Query("SELECT EXISTS(SELECT id FROM photos_drafts)")
    suspend fun hasPhotosDrafts(): Boolean

    @Query("SELECT * FROM photos_drafts")
    fun getPhotosDrafts(): Flow<List<PhotoDraftDto>>

    @Query("DELETE FROM photos_drafts WHERE id = :id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("DELETE FROM photos_drafts WHERE id in (:ids)")
    suspend fun deleteBulk(ids: List<Long>)

    @Query("DELETE FROM photos_drafts")
    suspend fun deleteAll()
}
