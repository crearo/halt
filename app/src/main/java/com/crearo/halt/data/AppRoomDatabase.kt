package com.crearo.halt.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UnlockStat::class], version = 1)
@TypeConverters(AppTypeConverters::class)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun unlockStatDao(): UnlockStatDao
}