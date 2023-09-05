package com.bakjoul.realestatemanager.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bakjoul.realestatemanager.data.photos.PhotoDao
import com.bakjoul.realestatemanager.data.property.PropertyDao
import com.bakjoul.realestatemanager.data.property.model.PropertyDtoEntity
import com.bakjoul.realestatemanager.data.photos.model.PhotoDtoEntity
import com.bakjoul.realestatemanager.data.property.model.PropertyType
import com.bakjoul.realestatemanager.data.utils.type_converters.BigDecimalTypeConverter
import com.bakjoul.realestatemanager.data.utils.type_converters.LocalDateTypeConverter
import com.google.gson.Gson
import java.math.BigDecimal
import java.time.LocalDate

@Database(
    entities = [PropertyDtoEntity::class, PhotoDtoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    LocalDateTypeConverter::class,
    BigDecimalTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getPropertyDao(): PropertyDao
    abstract fun getPhotoDao(): PhotoDao

    companion object {
        private const val DATABASE_NAME = "RealEstateManager_database"

        fun create(
            application: Application,
            workManager: WorkManager,
            gson: Gson,
        ): AppDatabase {
            val builder = Room.databaseBuilder(
                application,
                AppDatabase::class.java,
                DATABASE_NAME
            )

            builder.addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val propertiesAsJson = gson.toJson(
                        listOf(
                            PropertyDtoEntity(
                                id = 1,
                                type = PropertyType.Flat.name,
                                entryDate = LocalDate.parse("2023-01-01"),
                                saleDate = LocalDate.parse("2023-01-02"),
                                price = BigDecimal(100000),
                                surface = 100.0,
                                rooms = 5,
                                bathrooms = 2,
                                bedrooms = 3,
                                poiAirport = false,
                                poiBus = true,
                                poiHospital = false,
                                poiPark = true,
                                poiRestaurant = true,
                                poiSchool = true,
                                poiStore = true,
                                poiSubway = false,
                                poiTrain = true,
                                poiTramway = false,
                                address = "740 Park Avenue",
                                apartment = "6/7A",
                                zipcode = "NY 10021",
                                city = "New York",
                                state = "New York",
                                country = "United States",
                                latitude = 48.856614,
                                longitude = 2.3522219,
                                description = "Anchored by a vast marble gallery with sweeping staircase, the entertaining floor includes a baronial living room facing Park Avenue, handsome library with original paneling, and tremendous dining room; all of which enjoy fireplaces. The state-of-the-art St. Charles designed kitchen includes a sunny breakfast room and staff quarters. Upstairs, the expansive master suite overlooks Park Avenue and includes two marble baths, two dressing rooms, and two offices. Additionally there are three large bedrooms with en-suite baths and a media room.",
                                agent = "John Doe"
                            ),
                            PropertyDtoEntity(
                                id = 2,
                                type = PropertyType.House.name,
                                entryDate = LocalDate.parse("2023-02-01"),
                                saleDate = null,
                                price = BigDecimal(200000),
                                surface = 200.0,
                                rooms = 10,
                                bathrooms = 4,
                                bedrooms = 4,
                                poiAirport = true,
                                poiBus = true,
                                poiHospital = true,
                                poiPark = true,
                                poiRestaurant = true,
                                poiSchool = true,
                                poiStore = true,
                                poiSubway = false,
                                poiTrain = true,
                                poiTramway = true,
                                address = "2 rue de la paix",
                                apartment = "2",
                                zipcode = "75000",
                                city = "Paris",
                                state = "Ile de France",
                                country = "France",
                                latitude = 48.856614,
                                longitude = 2.3522219,
                                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                                agent = "Jane Doe"
                            ),
                            PropertyDtoEntity(
                                id = 3,
                                type = PropertyType.Duplex.name,
                                entryDate = LocalDate.parse("2023-04-01"),
                                saleDate = null,
                                price = BigDecimal(300000),
                                surface = 300.0,
                                rooms = 12,
                                bathrooms = 5,
                                bedrooms = 5,
                                poiAirport = false,
                                poiBus = true,
                                poiHospital = true,
                                poiPark = true,
                                poiRestaurant = true,
                                poiSchool = true,
                                poiStore = true,
                                poiSubway = false,
                                poiTrain = true,
                                poiTramway = true,
                                address = "20 rue de la paix",
                                apartment = "22",
                                zipcode = "75000",
                                city = "Paris",
                                state = "Ile de France",
                                country = "France",
                                latitude = 48.856614,
                                longitude = 2.3522219,
                                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                                agent = "Jane Doe"
                            ),
                        )
                    )

                    val photosAsJson = gson.toJson(
                        listOf(
                            PhotoDtoEntity(
                                propertyId = 1,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDtoEntity(
                                propertyId = 1,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDtoEntity(
                                propertyId = 1,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDtoEntity(
                                propertyId = 1,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDtoEntity(
                                propertyId = 1,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDtoEntity(
                                propertyId = 2,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDtoEntity(
                                propertyId = 3,
                                url = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                        )
                    )

                    workManager.enqueue(
                        OneTimeWorkRequestBuilder<InitializeDatabaseWorker>()
                            .setInputData(
                                workDataOf(
                                    InitializeDatabaseWorker.KEY_INPUT_DATA_PROPERTIES to propertiesAsJson,
                                    InitializeDatabaseWorker.KEY_INPUT_DATA_PHOTOS to photosAsJson
                                )
                            )
                            .build()
                    )
                }
            })
            return builder.build()
        }
    }
}
