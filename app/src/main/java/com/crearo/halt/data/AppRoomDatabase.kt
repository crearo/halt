package com.crearo.halt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UnlockStat::class], version = 1, exportSchema = false)
@TypeConverters(AppTypeConverters::class)
abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun unlockStatDao(): UnlockStatDao

    companion object {

        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "app_room_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}