package com.crearo.halt.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

// FIXME: Remove this with Hilt. This is an ugly mf
object UnlockStatDaoDbInitHelper {
    fun createDb(): AppRoomDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AppRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}