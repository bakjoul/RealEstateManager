package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import com.bakjoul.realestatemanager.data.property.model.PropertyDto
import com.bakjoul.realestatemanager.data.property.model.PropertyWithPhotosDto
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepositoryRoom @Inject constructor(
    private val propertyDao: PropertyDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PropertyRepository {

    override suspend fun addProperty(propertyEntity: PropertyEntity): Long = withContext(coroutineDispatcherProvider.io) {
        propertyDao.insert(mapToDtoEntity(propertyEntity))
    }

    override fun getPropertiesFlow(): Flow<List<PropertyEntity>> = propertyDao.getProperties().map { propertyWithPhotosList ->
        propertyWithPhotosList.map {
            mapToDomainEntity(it)
        }
    }.flowOn(coroutineDispatcherProvider.io)

    override fun getPropertyById(id: Long): Flow<PropertyEntity?> = propertyDao.getPropertyById(id).map {
        it?.let { mapToDomainEntity(it) }
    }.flowOn(coroutineDispatcherProvider.io)

    // region Mapping
    private fun mapToDtoEntity(propertyEntity: PropertyEntity): PropertyDto =
        PropertyDto(
            type = propertyEntity.type,
            entryDate = propertyEntity.entryDate,
            saleDate = propertyEntity.saleDate,
            price = propertyEntity.price,
            surface = propertyEntity.surface,
            rooms = propertyEntity.rooms,
            bathrooms = propertyEntity.bathrooms,
            bedrooms = propertyEntity.bedrooms,
            poiAirport = propertyEntity.amenities.contains(PropertyPoiEntity.AIRPORT),
            poiBus = propertyEntity.amenities.contains(PropertyPoiEntity.BUS),
            poiHospital = propertyEntity.amenities.contains(PropertyPoiEntity.HOSPITAL),
            poiPark = propertyEntity.amenities.contains(PropertyPoiEntity.PARK),
            poiRestaurant = propertyEntity.amenities.contains(PropertyPoiEntity.RESTAURANT),
            poiSchool = propertyEntity.amenities.contains(PropertyPoiEntity.SCHOOL),
            poiStore = propertyEntity.amenities.contains(PropertyPoiEntity.STORE),
            poiSubway = propertyEntity.amenities.contains(PropertyPoiEntity.SUBWAY),
            poiTrain = propertyEntity.amenities.contains(PropertyPoiEntity.TRAIN),
            poiTramway = propertyEntity.amenities.contains(PropertyPoiEntity.TRAMWAY),
            address = propertyEntity.fullAddress.address,
            apartment = propertyEntity.fullAddress.apartment,
            zipcode = propertyEntity.fullAddress.zipcode,
            city = propertyEntity.fullAddress.city,
            state = propertyEntity.fullAddress.state,
            country = propertyEntity.fullAddress.country,
            latitude = propertyEntity.latitude,
            longitude = propertyEntity.longitude,
            description = propertyEntity.description,
            agent = propertyEntity.agent,
        )

    private fun mapToDomainEntity(propertyWithPhotosDto: PropertyWithPhotosDto) : PropertyEntity {
        val details = propertyWithPhotosDto.propertyDto

        return PropertyEntity(
            id = details.id,
            type = details.type,
            entryDate = details.entryDate,
            saleDate = details.saleDate,
            price = details.price,
            surface = details.surface,
            rooms = details.rooms,
            bathrooms = details.bathrooms,
            bedrooms = details.bedrooms,
            amenities = mapAmenities(propertyWithPhotosDto),
            fullAddress = PropertyAddressEntity(
                address = details.address,
                apartment = details.apartment,
                zipcode = details.zipcode,
                city = details.city,
                state = details.state,
                country = details.country),
            latitude = details.latitude,
            longitude = details.longitude,
            description = details.description,
            photos = mapPhotos(propertyWithPhotosDto.photos),
            agent = details.agent
        )
    }

    private fun mapAmenities(propertyWithPhotosDto: PropertyWithPhotosDto): List<PropertyPoiEntity> = buildList {
        if (propertyWithPhotosDto.propertyDto.poiAirport) add(PropertyPoiEntity.AIRPORT)
        if (propertyWithPhotosDto.propertyDto.poiBus) add(PropertyPoiEntity.BUS)
        if (propertyWithPhotosDto.propertyDto.poiHospital) add(PropertyPoiEntity.HOSPITAL)
        if (propertyWithPhotosDto.propertyDto.poiPark) add(PropertyPoiEntity.PARK)
        if (propertyWithPhotosDto.propertyDto.poiRestaurant) add(PropertyPoiEntity.RESTAURANT)
        if (propertyWithPhotosDto.propertyDto.poiSchool) add(PropertyPoiEntity.SCHOOL)
        if (propertyWithPhotosDto.propertyDto.poiStore) add(PropertyPoiEntity.STORE)
        if (propertyWithPhotosDto.propertyDto.poiSubway) add(PropertyPoiEntity.SUBWAY)
        if (propertyWithPhotosDto.propertyDto.poiTrain) add(PropertyPoiEntity.TRAIN)
        if (propertyWithPhotosDto.propertyDto.poiTramway) add(PropertyPoiEntity.TRAMWAY)
    }

    private fun mapPhotos(photos: List<PhotoDto>): List<PhotoEntity> =
        photos.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyId,
                url = it.url,
                description = it.description
            )
        }
    // endregion Mapping
}
