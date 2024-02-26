package com.bakjoul.realestatemanager.data.photos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photos: List<PhotoDto>): List<Long>

    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoDto>>

    @Query("DELETE FROM photos WHERE id IN (:idList)")
    suspend fun delete(idList: List<Long>)

    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deleteAllPhotosForPropertyId(propertyId: Long)
}
