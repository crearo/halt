package com.crearo.halt.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Duration
import java.time.Instant.ofEpochSecond

@RunWith(AndroidJUnit4::class)
class UnlockStatDaoRepoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var db: AppRoomDatabase
    private lateinit var unlockStatDao: UnlockStatDao
    private lateinit var repository: UnlockStatRepository

    // todo this entire setup should have been done by the shiny new Hilt
    @Before
    fun createDb() {
        db = UnlockStatDaoDbInitHelper.createDb()
        unlockStatDao = db.unlockStatDao()
        repository = UnlockStatRepository(unlockStatDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun insertTestData() {
        repository.addNewUnlock(ofEpochSecond(1)).test().await()
        repository.addNewLock(ofEpochSecond(3)).test().await() // 2 seconds
        repository.addNewUnlock(ofEpochSecond(5)).test().await()
        repository.addNewLock(ofEpochSecond(10)).test().await() // 7 seconds
        repository.addNewUnlock(ofEpochSecond(20)).test().await()
        repository.addNewLock(ofEpochSecond(30)).test().await() // 17 seconds
        repository.addNewUnlock(ofEpochSecond(32)).test().await()
        repository.addNewLock(ofEpochSecond(34)).test().await() // 19 seconds
        // the last one doesn't have a corresponding lock
        repository.addNewUnlock(ofEpochSecond(40)).test().await()
    }

    @Test
    fun testInsertData() {
        unlockStatDao.deleteAll()
        repository.getUnlockStats()
            .test()
            .awaitCount(1)
            .assertValue { it.isEmpty() }
        insertTestData()
        repository.getUnlockStats()
            .test()
            .awaitCount(1)
            .assertValue { it.size == 5 }
    }

    /*
     * So I didn't quite know how to write this without hardcoding values. But this works great!
     * Case 1: before first unlock and after last lock
     * Case 2: before first unlock and after last unlock (no lock at the end)
     * Case 3: in between an unlock and lock, and before another unlock
     * Case 4: before an unlock, and in between an unlock and lock
     * Case 5: before and unlock, and after a lock
     */

    @Test
    fun testTotalTimeUsed_beforeFirstUnlock_and_afterLastLock() {
        testTotalTimeUsed(0, 35, 19)
    }

    @Test
    fun testTotalTimeUsed_beforeFirstUnlock_and_afterLastUnlock() {
        testTotalTimeUsed(0, 45, 24)
    }

    @Test
    fun testTotalTimeUsed_inBetweenAnUnlockAndLock_and_beforeAnUnlock() {
        testTotalTimeUsed(7, 35, 15)
    }

    @Test
    fun testTotalTimeUsed_inBeforeAnUnlock_and_betweenAnUnlockAndLock() {
        testTotalTimeUsed(11, 25, 5)
    }

    @Test
    fun testTotalTimeUsed_inBeforeAnUnlock_and_afterALock() {
        testTotalTimeUsed(15, 35, 12)
    }

    private fun testTotalTimeUsed(startTime: Long, endTime: Long, expectedTime: Long) {
        unlockStatDao.deleteAll()
        insertTestData()
        repository.getTotalTimeUsed(ofEpochSecond(startTime), ofEpochSecond(endTime))
            .test()
            .await()
            .assertValue(Duration.ofSeconds(expectedTime))
    }

}