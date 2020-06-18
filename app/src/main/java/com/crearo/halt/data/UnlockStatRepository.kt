package com.crearo.halt.data

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset.UTC
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UnlockStatRepository @Inject constructor(private val unlockStatDao: UnlockStatDao) {

    /**
     * todo check:
     * 1. We aren't inserting a new unlock before inserting the previous lock
     * 2. The insert is sequential and monotonically increasing
     *
     * todo: i also think the repository should handle the case where shit goes down and we are
     * unable to save things because of some failure. It isn't like the activity will do that.
     * Thazz a function of here. But, we should still pass on that info in case activity decides to
     * do something about it.
     **/
    fun addNewUnlock(unlockInstant: Instant): Completable {
        return unlockStatDao
            .insertNewUnlock(UnlockStat(unlockInstant))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
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
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    fun getTotalTimeUsed(startTime: Instant, endTime: Instant): Single<Long> {
        return unlockStatDao.getUnlockStats(startTime, endTime)
            .map { list ->
                list.stream().mapToLong {
                    TimeUnit.MILLISECONDS.toSeconds(
                        max(startTime, it.unlockTime)
                                + min(endTime, it.lockTime!!)
                    )
                }.sum()
            }
    }

    /**
     * Defined as the first time the phone was checked after 5am after having not used the phone
     * for an hour. Note, here we have to convert the user's 5am to UTC because that's what things
     * are stored in.
     * @return error if the first unlock of the day hasn't happened yet
     **/
    fun getFirstUnlockPost5AM(): Single<() -> UnlockStat> {
        val fiveAmTodayInUTC = LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0))
            .toInstant(UTC)
        if (now().isBefore(fiveAmTodayInUTC)) {
            return Single.error(IllegalStateException("now() is before 5am today"))
        }
        return unlockStatDao
            .getUnlockStats(fiveAmTodayInUTC, now())
            .map { list -> { list.first() } }
    }

    private fun max(val1: Instant, val2: Instant): Long {
        return max(val1.toEpochMilli(), val2.toEpochMilli())
    }

    private fun min(val1: Instant, val2: Instant): Long {
        return min(val1.toEpochMilli(), val2.toEpochMilli())
    }
}