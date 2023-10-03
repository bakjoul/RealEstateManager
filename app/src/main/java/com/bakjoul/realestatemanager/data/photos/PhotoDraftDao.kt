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
    suspend fun insert(photo: PhotoDraftDto): Long?

    @Query("SELECT EXISTS(SELECT id FROM photo_drafts)")
    suspend fun hasPhotoDrafts(): Boolean

    @Query("SELECT * FROM photo_drafts")
    fun getPhotoDrafts(): Flow<List<PhotoDraftDto>>

    @Query("DELETE FROM photo_drafts WHERE id = :id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("DELETE FROM photo_drafts WHERE id in (:ids)")
    suspend fun deleteBulk(ids: List<Long>)

    @Query("DELETE FROM photo_drafts")
    suspend fun deleteAll()
}
