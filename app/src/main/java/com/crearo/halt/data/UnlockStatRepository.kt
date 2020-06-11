package com.crearo.halt.data

import io.reactivex.Completable
import io.reactivex.Flowable
import java.time.Instant
import javax.inject.Inject

class UnlockStatRepository @Inject constructor(private val unlockStatDao: UnlockStatDao) {

    /**
     * todo check:
     * 1. We aren't inserting a new unlock before inserting the previous lock
     * 2. The insert is sequential and monotonically increasing
     **/
    fun addNewUnlock(unlockInstant: Instant): Completable {
        return unlockStatDao.insertNewUnlock(UnlockStat(unlockInstant))
    }

    /**
     * todo I wanna know what happens if there are elements before. Does that get propagated through an error from Single->Completable?
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

    fun getUnlockStats(): Flowable<List<UnlockStat>> {
        return unlockStatDao.getUnlockStats()
    }
}