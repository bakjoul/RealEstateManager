package com.bakjoul.realestatemanager.data.photos.edit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bakjoul.realestatemanager.data.photos.model.TemporaryPhotoDto
import kotlinx.coroutines.flow.Flow

@Dao
interface TemporaryPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photos: List<TemporaryPhotoDto>): List<Long>

    @Query("SELECT * FROM temporary_photos WHERE property_id = :propertyId")
    fun getPhotosForPropertyId(propertyId: Long): Flow<List<TemporaryPhotoDto>>

    @Query("DELETE FROM temporary_photos WHERE id IN (:idList)")
    suspend fun delete(idList: List<Long>): Int

    @Query("DELETE FROM temporary_photos WHERE property_id = :propertyId")
    suspend fun deleteAllPhotosForPropertyId(propertyId: Long): Int

    @Query("UPDATE temporary_photos SET description = :description WHERE id = :photoId")
    suspend fun updatePhotoDescription(photoId: Long, description: String): Int
}
