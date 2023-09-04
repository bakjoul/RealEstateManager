package com.bakjoul.realestatemanager.data.photos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photoEntity: PhotoEntity)

    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotos(propertyId: Long): Flow<List<PhotoEntity>>
}