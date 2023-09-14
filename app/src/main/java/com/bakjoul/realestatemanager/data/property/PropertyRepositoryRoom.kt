package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import com.bakjoul.realestatemanager.data.property.model.PropertyDto
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.data.property.model.PropertyWithPhotosDto
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.Dispatchers
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
    }

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
            poiAirport = propertyEntity.amenities.contains(PropertyPoiEntity.Airport),
            poiBus = propertyEntity.amenities.contains(PropertyPoiEntity.Bus),
            poiHospital = propertyEntity.amenities.contains(PropertyPoiEntity.Hospital),
            poiPark = propertyEntity.amenities.contains(PropertyPoiEntity.Park),
            poiRestaurant = propertyEntity.amenities.contains(PropertyPoiEntity.Restaurant),
            poiSchool = propertyEntity.amenities.contains(PropertyPoiEntity.School),
            poiStore = propertyEntity.amenities.contains(PropertyPoiEntity.Store),
            poiSubway = propertyEntity.amenities.contains(PropertyPoiEntity.Subway),
            poiTrain = propertyEntity.amenities.contains(PropertyPoiEntity.Train),
            poiTramway = propertyEntity.amenities.contains(PropertyPoiEntity.Tramway),
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
        if (propertyWithPhotosDto.propertyDto.poiAirport) add(PropertyPoiEntity.Airport)
        if (propertyWithPhotosDto.propertyDto.poiBus) add(PropertyPoiEntity.Bus)
        if (propertyWithPhotosDto.propertyDto.poiHospital) add(PropertyPoiEntity.Hospital)
        if (propertyWithPhotosDto.propertyDto.poiPark) add(PropertyPoiEntity.Park)
        if (propertyWithPhotosDto.propertyDto.poiRestaurant) add(PropertyPoiEntity.Restaurant)
        if (propertyWithPhotosDto.propertyDto.poiSchool) add(PropertyPoiEntity.School)
        if (propertyWithPhotosDto.propertyDto.poiStore) add(PropertyPoiEntity.Store)
        if (propertyWithPhotosDto.propertyDto.poiSubway) add(PropertyPoiEntity.Subway)
        if (propertyWithPhotosDto.propertyDto.poiTrain) add(PropertyPoiEntity.Train)
        if (propertyWithPhotosDto.propertyDto.poiTramway) add(PropertyPoiEntity.Tramway)
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
