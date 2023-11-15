package com.bakjoul.realestatemanager.data.photos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: PhotoDto): Long

    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoDto>>

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deleteAllPhotosForPropertyId(propertyId: Long)
}
