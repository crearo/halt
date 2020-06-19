package com.crearo.halt.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant.ofEpochMilli

@RunWith(AndroidJUnit4::class)
class UnlockStatDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var db: AppRoomDatabase
    private lateinit var unlockStatDao: UnlockStatDao

    @Before
    fun createDb() {
        db = UnlockStatDaoDbInitHelper.createDb()
        unlockStatDao = db.unlockStatDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertUnlockAndUpdateLock() {
        var unlockStat = UnlockStat(ofEpochMilli(0))
        unlockStatDao.insertNewUnlock(unlockStat).blockingAwait()

        // after some point the user will lock their phone, at which point we'd like to update the
        // lock status todo this should really be done in the Repository not directly to the db
        unlockStat = unlockStatDao.getLastUnlock().blockingGet()
        unlockStat.lockTime = ofEpochMilli(5)
        unlockStatDao.updateCorrespondingLock(unlockStat).blockingAwait()

        // ensure that there is a total of one entries
        unlockStatDao.getUnlockStats().test().assertValue { list -> list.size == 1 }
        unlockStatDao.getUnlockStats().test()
            .assertValue { list -> list[0].unlockTime.toEpochMilli() == 0L }
        unlockStatDao.getUnlockStats().test()
            .assertValue { list -> list[0].lockTime!!.toEpochMilli() == 5L }
    }

}