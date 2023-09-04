package com.bakjoul.realestatemanager.domain.photos

import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    fun addPhoto(photoUrl: String, description: String)

    fun getPhotos(): Flow<Map<String, String>>

    fun clearPhotos()
}
