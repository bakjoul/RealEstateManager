package com.bakjoul.realestatemanager.data.photos

import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import com.bakjoul.realestatemanager.data.photos.model.TemporaryPhotoDto
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class PhotoMapper @Inject constructor() {

    fun toPhotoDto(photoEntity: PhotoEntity): PhotoDto =
        PhotoDto(
            propertyId = photoEntity.propertyId,
            uri = photoEntity.uri,
            description = photoEntity.description
        )

    fun toTemporaryPhotoDto(photoEntity: PhotoEntity): TemporaryPhotoDto =
        TemporaryPhotoDto(
            propertyId = photoEntity.propertyId,
            uri = photoEntity.uri,
            description = photoEntity.description
        )

    fun dtoToDomainEntities(photoDtoList: List<PhotoDto>) =
        photoDtoList.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyId,
                uri = it.uri,
                description = it.description
            )
        }

    fun tempDtoToDomainEntities(tempPhotoDtoList: List<TemporaryPhotoDto>) =
        tempPhotoDtoList.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyId,
                uri = it.uri,
                description = it.description
            )
        }
}
