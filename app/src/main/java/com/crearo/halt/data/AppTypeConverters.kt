package com.crearo.halt.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.Instant.ofEpochMilli

class AppTypeConverters {

    @TypeConverter
    fun fromInstant(value: Instant? = null): Long {
        return value?.toEpochMilli() ?: -1
    }

    @TypeConverter
    fun toInstant(value: Long): Instant {
        return ofEpochMilli(value)
    }

}