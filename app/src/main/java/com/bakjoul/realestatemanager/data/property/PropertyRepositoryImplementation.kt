package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepositoryImplementation @Inject constructor() : PropertyRepository {
    private val propertiesMutableStateFlow: MutableStateFlow<List<PropertyEntity>> =
        MutableStateFlow(
            listOf(
                PropertyEntity(
                    id = 0,
                    type = PropertyType.Flat.name,
                    price = 100000,
                    surface = 100,
                    rooms = 5,
                    bedrooms = 3,
                    bathrooms = 2,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                    address = "1 rue de la paix",
                    appartment = "1",
                    zipcode = 75000,
                    city = "Paris",
                    state = "Ile de France",
                    country = "France",
                    latitude = 48.856614,
                    longitude = 2.3522219,
                    poiSchool = true,
                    poiStore = true,
                    poiPark = true,
                    poiRestaurant = true,
                    poiTrain = true,
                    poiBus = true,
                    poiAirport = true,
                    isSold = false,
                    entryDate = LocalDate.parse("2023-01-01"),
                    soldDate = null,
                    agent = "John Doe"
                ),
                PropertyEntity(
                    id = 1,
                    type = PropertyType.House.name,
                    price = 200000,
                    surface = 200,
                    rooms = 10,
                    bedrooms = 5,
                    bathrooms = 3,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                    address = "2 rue de la paix",
                    appartment = "2",
                    zipcode = 75000,
                    city = "Paris",
                    state = "Ile de France",
                    country = "France",
                    latitude = 48.856614,
                    longitude = 2.3522219,
                    poiSchool = true,
                    poiStore = true,
                    poiPark = true,
                    poiRestaurant = true,
                    poiTrain = true,
                    poiBus = true,
                    poiAirport = true,
                    isSold = false,
                    entryDate = LocalDate.parse("2023-02-01"),
                    soldDate = null,
                    agent = "Jane Doe"
                )
            )
        )

    override fun getPropertiesFlow(): Flow<List<PropertyEntity>> =
        propertiesMutableStateFlow.asStateFlow()

    override suspend fun getPropertyById(id: Long): PropertyEntity? = withContext(Dispatchers.IO) {
        propertiesMutableStateFlow.value.find { it.id == id }
    }
}
