package com.crearo.halt.di

import android.content.Context
import androidx.room.Room
import com.crearo.halt.data.AppRoomDatabase
import com.crearo.halt.data.UnlockStatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppRoomDatabase {
        return Room.databaseBuilder(context, AppRoomDatabase::class.java, "app_room_database.db")
            .build()
    }

    @Provides
    fun provideUnlockStatDao(database: AppRoomDatabase): UnlockStatDao {
        return database.unlockStatDao()
    }

}