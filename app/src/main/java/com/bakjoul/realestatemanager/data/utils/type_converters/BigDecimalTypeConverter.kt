package com.bakjoul.realestatemanager.data.utils.type_converters

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalTypeConverter {
    @TypeConverter
    fun bigDecimalToString(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.toString()
    }

    @TypeConverter
    fun stringToBigDecimal(bigDecimalString: String?): BigDecimal {
        return BigDecimal(bigDecimalString)
    }
}
