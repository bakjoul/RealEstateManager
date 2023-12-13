package com.bakjoul.realestatemanager.data.utils.type_converters

import androidx.room.TypeConverter
import java.time.LocalDateTime

class LocalDateTimeTypeConverter {
    @TypeConverter
    fun localDateTimeToString(localDateTime: LocalDateTime?): String? = localDateTime?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}
