package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyWithPhotosEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepositoryRoom @Inject constructor(
    private val propertyDao: PropertyDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PropertyRepository {

    override suspend fun add(propertyEntity: PropertyEntity): Long = withContext(coroutineDispatcherProvider.io) {
        propertyDao.insert(mapToDtoEntity(propertyEntity))
    }

    override fun getPropertiesFlow(): Flow<List<PropertyEntity>> {
        return propertyDao.getProperties().map { propertyWithPhotosList ->
            propertyWithPhotosList.map {
                mapToDomainEntity(it)
            }
        }
    }

    override fun getPropertyById(id: Long): PropertyEntity? {
        return propertyDao.getPropertyById(id)?.let {
            mapToDomainEntity(it)
        }
    }

    // region Mapping
    private fun mapToDtoEntity(propertyEntity: PropertyEntity): PropertyDtoEntity =
        PropertyDtoEntity(
            type = propertyEntity.type,
            entryDate = propertyEntity.entryDate,
            saleDate = propertyEntity.saleDate,
            price = propertyEntity.price,
            surface = propertyEntity.surface,
            rooms = propertyEntity.rooms,
            bathrooms = propertyEntity.bathrooms,
            bedrooms = propertyEntity.bedrooms,
            poiAirport = propertyEntity.amenities.contains(PropertyPoi.Airport),
            poiBus = propertyEntity.amenities.contains(PropertyPoi.Bus),
            poiHospital = propertyEntity.amenities.contains(PropertyPoi.Hospital),
            poiPark = propertyEntity.amenities.contains(PropertyPoi.Park),
            poiRestaurant = propertyEntity.amenities.contains(PropertyPoi.Restaurant),
            poiSchool = propertyEntity.amenities.contains(PropertyPoi.School),
            poiStore = propertyEntity.amenities.contains(PropertyPoi.Store),
            poiSubway = propertyEntity.amenities.contains(PropertyPoi.Subway),
            poiTrain = propertyEntity.amenities.contains(PropertyPoi.Train),
            poiTramway = propertyEntity.amenities.contains(PropertyPoi.Tramway),
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

    private fun mapToDomainEntity(propertyWithPhotosEntity: PropertyWithPhotosEntity) : PropertyEntity {
        val details = propertyWithPhotosEntity.propertyDtoEntity

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
            amenities = mapAmenities(propertyWithPhotosEntity),
            fullAddress = PropertyAddress(
                address = details.address,
                apartment = details.apartment,
                zipcode = details.zipcode,
                city = details.city,
                state = details.state,
                country = details.country),
            latitude = details.latitude,
            longitude = details.longitude,
            description = details.description,
            photos = propertyWithPhotosEntity.photos,
            agent = details.agent
        )
    }

    private fun mapAmenities(propertyWithPhotosEntity: PropertyWithPhotosEntity): List<PropertyPoi> {
        val details = propertyWithPhotosEntity.propertyDtoEntity
        return mutableListOf<PropertyPoi>().apply {
            if (details.poiAirport) add(PropertyPoi.Airport)
            if (details.poiBus) add(PropertyPoi.Bus)
            if (details.poiHospital) add(PropertyPoi.Hospital)
            if (details.poiPark) add(PropertyPoi.Park)
            if (details.poiRestaurant) add(PropertyPoi.Restaurant)
            if (details.poiSchool) add(PropertyPoi.School)
            if (details.poiStore) add(PropertyPoi.Store)
            if (details.poiSubway) add(PropertyPoi.Subway)
            if (details.poiTrain) add(PropertyPoi.Train)
            if (details.poiTramway) add(PropertyPoi.Tramway)
        }
    }
    // endregion Mapping
}
