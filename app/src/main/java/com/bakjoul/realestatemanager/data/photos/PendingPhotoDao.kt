package com.bakjoul.realestatemanager.data.photos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bakjoul.realestatemanager.data.photos.model.PendingPhotoDtoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPhotoDao {

    @Insert
    suspend fun insert(photo: PendingPhotoDtoEntity)

    @Query("SELECT EXISTS(SELECT id FROM pending_photos)")
    suspend fun hasPendingPhotos(): Boolean

    @Query("SELECT * FROM pending_photos")
    fun getPendingPhotos(): Flow<List<PendingPhotoDtoEntity>>

    @Query("DELETE FROM pending_photos WHERE id = :id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("DELETE FROM pending_photos WHERE id in (:ids)")
    suspend fun deleteBulk(ids: List<Long>)

    @Query("DELETE FROM pending_photos")
    suspend fun deleteAll()
}
