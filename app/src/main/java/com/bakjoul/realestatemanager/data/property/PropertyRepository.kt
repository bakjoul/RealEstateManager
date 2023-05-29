package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.domain.property.PropertyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepository @Inject constructor() {
    private val propertiesMutableStateFlow: MutableStateFlow<List<PropertyEntity>> = MutableStateFlow(
            listOf(
                PropertyEntity(
                    id = 0,
                    type = PropertyType.FLAT.name,
                    price = 100000,
                    surface = 100,
                    rooms = 5,
                    bedrooms = 3,
                    bathrooms = 2,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                    address = "1 rue de la paix",
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
                    type = PropertyType.HOUSE.name,
                    price = 200000,
                    surface = 200,
                    rooms = 10,
                    bedrooms = 5,
                    bathrooms = 3,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                    address = "2 rue de la paix",
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

    val properttiesStateFlow: StateFlow<List<PropertyEntity>> = propertiesMutableStateFlow.asStateFlow()

    fun getPropertyById(id: Long): PropertyEntity? = propertiesMutableStateFlow.value.find { it.id == id }
}
