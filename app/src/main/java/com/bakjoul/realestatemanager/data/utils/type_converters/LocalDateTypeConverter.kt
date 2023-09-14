package com.bakjoul.realestatemanager.data.utils.type_converters

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateTypeConverter {
    @TypeConverter
    fun localDateToString(localDate: LocalDate?): String? = localDate?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
