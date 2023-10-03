package com.bakjoul.realestatemanager.data.property

import android.database.sqlite.SQLiteException
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import com.bakjoul.realestatemanager.data.property.model.PropertyDto
import com.bakjoul.realestatemanager.data.property.model.PropertyFormDto
import com.bakjoul.realestatemanager.data.property.model.PropertyFormWithPhotosDto
import com.bakjoul.realestatemanager.data.property.model.PropertyWithPhotosDto
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PropertyAddressEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepositoryRoom @Inject constructor(
    private val propertyDao: PropertyDao,
    private val propertyFormDao: PropertyFormDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PropertyRepository {

    override suspend fun addProperty(propertyEntity: PropertyEntity): Long? = withContext(coroutineDispatcherProvider.io) {
        try {
            propertyDao.insert(mapPropertyEntityToDto(propertyEntity))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override fun getPropertiesFlow(): Flow<List<PropertyEntity>> = propertyDao.getProperties().map { propertyWithPhotosList ->
        propertyWithPhotosList.map {
            mapToDomainEntity(it)
        }
    }.flowOn(coroutineDispatcherProvider.io)

    override fun getPropertyById(id: Long): Flow<PropertyEntity?> = propertyDao.getPropertyById(id).map {
        it?.let { mapToDomainEntity(it) }
    }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun addPropertyDraft(propertyForm: PropertyFormEntity): Long? = withContext(coroutineDispatcherProvider.io) {
        propertyFormDao.insert(mapPropertyDraftToDto(propertyForm))
    }

    override suspend fun hasPropertyForms(): Boolean = propertyFormDao.hasPropertyForms()

    override fun getPropertyFormsFlow(): Flow<List<PropertyFormEntity>> = propertyFormDao.getPropertyForms().map { propertyFormWithPhotosList ->
        propertyFormWithPhotosList.map {
            mapPropertyFormDtoToDomainEntity(it)
        }
    }.flowOn(coroutineDispatcherProvider.io)

    override fun getPropertyFormById(id: Long): Flow<PropertyFormEntity?> = propertyFormDao.getPropertyFormById(id).map {
        it?.let { mapPropertyFormDtoToDomainEntity(it) }
    }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun deletePropertyForm(id: Long) = withContext(coroutineDispatcherProvider.io) {
        propertyFormDao.delete(id)
    }

    // region Mapping
    private fun mapPropertyEntityToDto(propertyEntity: PropertyEntity): PropertyDto =
        PropertyDto(
            type = propertyEntity.type,
            forSaleSince = propertyEntity.entryDate,
            dateOfSale = propertyEntity.saleDate,
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
            streetNumber = propertyEntity.address.streetNumber,
            route = propertyEntity.address.route,
            complementaryAddress = propertyEntity.address.complementaryAddress,
            zipcode = propertyEntity.address.zipcode,
            city = propertyEntity.address.city,
            state = propertyEntity.address.state,
            country = propertyEntity.address.country,
            latitude = propertyEntity.address.latitude,
            longitude = propertyEntity.address.longitude,
            description = propertyEntity.description,
            agent = propertyEntity.agent,
        )

    private fun mapToDomainEntity(propertyWithPhotosDto: PropertyWithPhotosDto) : PropertyEntity {
        return PropertyEntity(
            id = propertyWithPhotosDto.propertyDto.id,
            type = propertyWithPhotosDto.propertyDto.type,
            entryDate = propertyWithPhotosDto.propertyDto.forSaleSince,
            saleDate = propertyWithPhotosDto.propertyDto.dateOfSale,
            price = propertyWithPhotosDto.propertyDto.price,
            surface = propertyWithPhotosDto.propertyDto.surface,
            rooms = propertyWithPhotosDto.propertyDto.rooms,
            bathrooms = propertyWithPhotosDto.propertyDto.bathrooms,
            bedrooms = propertyWithPhotosDto.propertyDto.bedrooms,
            amenities = mapPropertyDtoAmenitiesToPoiListEntity(propertyWithPhotosDto.propertyDto),
            address = PropertyAddressEntity(
                streetNumber = propertyWithPhotosDto.propertyDto.streetNumber,
                route = propertyWithPhotosDto.propertyDto.route,
                complementaryAddress = propertyWithPhotosDto.propertyDto.complementaryAddress,
                zipcode = propertyWithPhotosDto.propertyDto.zipcode,
                city = propertyWithPhotosDto.propertyDto.city,
                state = propertyWithPhotosDto.propertyDto.state,
                country = propertyWithPhotosDto.propertyDto.country,
                latitude = propertyWithPhotosDto.propertyDto.latitude,
                longitude = propertyWithPhotosDto.propertyDto.longitude),
            description = propertyWithPhotosDto.propertyDto.description,
            photos = mapPhotos(propertyWithPhotosDto.photos),
            agent = propertyWithPhotosDto.propertyDto.agent
        )
    }

    private fun mapPropertyDtoAmenitiesToPoiListEntity(propertyDto: PropertyDto): List<PropertyPoiEntity> = buildList {
        if (propertyDto.poiAirport) add(PropertyPoiEntity.AIRPORT)
        if (propertyDto.poiBus) add(PropertyPoiEntity.BUS)
        if (propertyDto.poiHospital) add(PropertyPoiEntity.HOSPITAL)
        if (propertyDto.poiPark) add(PropertyPoiEntity.PARK)
        if (propertyDto.poiRestaurant) add(PropertyPoiEntity.RESTAURANT)
        if (propertyDto.poiSchool) add(PropertyPoiEntity.SCHOOL)
        if (propertyDto.poiStore) add(PropertyPoiEntity.STORE)
        if (propertyDto.poiSubway) add(PropertyPoiEntity.SUBWAY)
        if (propertyDto.poiTrain) add(PropertyPoiEntity.TRAIN)
        if (propertyDto.poiTramway) add(PropertyPoiEntity.TRAMWAY)
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

    private fun mapPropertyDraftToDto(propertyForm: PropertyFormEntity): PropertyFormDto =
        PropertyFormDto(
            type = propertyForm.type?.name.toString(),
            isSold = propertyForm.isSold,
            forSaleSince = propertyForm.forSaleSince,
            dateOfSale = propertyForm.dateOfSale,
            price = propertyForm.price,
            surface = propertyForm.surface,
            rooms = propertyForm.rooms,
            bathrooms = propertyForm.bathrooms,
            bedrooms = propertyForm.bedrooms,
            poiAirport = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.AIRPORT),
            poiBus = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.BUS),
            poiHospital = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.HOSPITAL),
            poiPark = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.PARK),
            poiRestaurant = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.RESTAURANT),
            poiSchool = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.SCHOOL),
            poiStore = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.STORE),
            poiSubway = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.SUBWAY),
            poiTrain = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.TRAIN),
            poiTramway = propertyForm.pointsOfInterest?.contains(PropertyPoiEntity.TRAMWAY),
            autoCompleteStreetNumber = propertyForm.autoCompleteAddress?.streetNumber,
            autoCompleteRoute = propertyForm.autoCompleteAddress?.route,
            autoCompleteComplementaryAddress = propertyForm.autoCompleteAddress?.complementaryAddress,
            autoCompleteZipcode = propertyForm.autoCompleteAddress?.zipcode,
            autoCompleteCity = propertyForm.autoCompleteAddress?.city,
            autoCompleteState = propertyForm.autoCompleteAddress?.state,
            autoCompleteCountry = propertyForm.autoCompleteAddress?.country,
            autoCompleteLatitude = propertyForm.autoCompleteAddress?.latitude,
            autoCompleteLongitude = propertyForm.autoCompleteAddress?.longitude,
            streetNumber = propertyForm.address?.streetNumber,
            route = propertyForm.address?.route,
            complementaryAddress = propertyForm.address?.complementaryAddress,
            zipcode = propertyForm.address?.zipcode,
            city = propertyForm.address?.city,
            state = propertyForm.address?.state,
            country = propertyForm.address?.country,
            latitude = propertyForm.address?.latitude,
            longitude = propertyForm.address?.longitude,
            description = propertyForm.description,
            agent = propertyForm.agent,
        )

    private fun mapPropertyFormDtoToDomainEntity(propertyFormWithPhotosDto: PropertyFormWithPhotosDto) : PropertyFormEntity =
        PropertyFormEntity(
            id = propertyFormWithPhotosDto.propertyFormDto.id,
            type = mapPropertyType(propertyFormWithPhotosDto.propertyFormDto.type),
            isSold = propertyFormWithPhotosDto.propertyFormDto.isSold,
            forSaleSince = propertyFormWithPhotosDto.propertyFormDto.forSaleSince,
            dateOfSale = propertyFormWithPhotosDto.propertyFormDto.dateOfSale,
            price = propertyFormWithPhotosDto.propertyFormDto.price,
            surface = propertyFormWithPhotosDto.propertyFormDto.surface,
            rooms = propertyFormWithPhotosDto.propertyFormDto.rooms,
            bathrooms = propertyFormWithPhotosDto.propertyFormDto.bathrooms,
            bedrooms = propertyFormWithPhotosDto.propertyFormDto.bedrooms,
            pointsOfInterest = mapPropertyFormDtoAmenitiesToPoiListEntity(propertyFormWithPhotosDto.propertyFormDto),
        )

    private fun mapPropertyFormDtoAmenitiesToPoiListEntity(propertyFormDto: PropertyFormDto): List<PropertyPoiEntity> = buildList {
        if (propertyFormDto.poiAirport == true) add(PropertyPoiEntity.AIRPORT)
        if (propertyFormDto.poiBus == true) add(PropertyPoiEntity.BUS)
        if (propertyFormDto.poiHospital == true) add(PropertyPoiEntity.HOSPITAL)
        if (propertyFormDto.poiPark == true) add(PropertyPoiEntity.PARK)
        if (propertyFormDto.poiRestaurant == true) add(PropertyPoiEntity.RESTAURANT)
        if (propertyFormDto.poiSchool == true) add(PropertyPoiEntity.SCHOOL)
        if (propertyFormDto.poiStore == true) add(PropertyPoiEntity.STORE)
        if (propertyFormDto.poiSubway == true) add(PropertyPoiEntity.SUBWAY)
        if (propertyFormDto.poiTrain == true) add(PropertyPoiEntity.TRAIN)
        if (propertyFormDto.poiTramway == true) add(PropertyPoiEntity.TRAMWAY)
    }

    private fun mapPropertyType(type: String?): PropertyTypeEntity? {
        return when (type) {
            PropertyTypeEntity.DUPLEX.name -> PropertyTypeEntity.DUPLEX
            PropertyTypeEntity.FLAT.name -> PropertyTypeEntity.FLAT
            PropertyTypeEntity.HOUSE.name -> PropertyTypeEntity.HOUSE
            PropertyTypeEntity.LOFT.name -> PropertyTypeEntity.LOFT
            PropertyTypeEntity.OTHER.name -> PropertyTypeEntity.OTHER
            PropertyTypeEntity.PENTHOUSE.name -> PropertyTypeEntity.PENTHOUSE
            else -> null
        }
    }
    // endregion Mapping
}
