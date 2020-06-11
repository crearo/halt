package com.crearo.halt.data

import io.reactivex.Completable
import java.time.Instant

class UnlockStatRepository(private val unlockStatDao: UnlockStatDao) {

    /**
     * todo check:
     * 1. We aren't inserting a new unlock before inserting the previous lock
     * 2. The insert is sequential and monotonically increasing
     **/
    fun addNewUnlock(unlockInstant: Instant): Completable {
        return unlockStatDao.insertNewUnlock(UnlockStat(unlockInstant))
    }

    /**
     * todo I wanna know what happens if there are elements before. Does that get propogated through an error from Single->Completable?
     * That'd be awesome.
     **/
    fun addNewLock(lockInstant: Instant): Completable {
        return unlockStatDao
            .getLastUnlock()
            .flatMapCompletable { unlockStat ->
                unlockStat.lockTime = lockInstant
                unlockStatDao.updateCorrespondingLock(unlockStat)
            }
    }
}