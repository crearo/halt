package com.crearo.halt.data

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.time.*
import java.time.ZoneOffset.UTC
import javax.inject.Inject

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
    fun addNewUnlock(unlockInstantUtc: Instant): Completable {
        return unlockStatDao
            .insertNewUnlock(UnlockStat(unlockInstantUtc))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    /**
     * todo I wanna know what happens if there are elements before. Does that get propagated through an error from Single->Completable?
     * That'd be awesome.
     **/
    fun addNewLock(lockInstantUtc: Instant): Completable {
        return unlockStatDao
            .getLastUnlock()
            .flatMapCompletable { unlockStat ->
                unlockStat.lockTime = lockInstantUtc
                unlockStatDao.updateCorrespondingLock(unlockStat)
            }
    }

    fun getUnlockStats(): Flowable<List<UnlockStat>> {
        return unlockStatDao.getUnlockStats()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    /**
     * @return total time the phone is used between the given parameters. This accounts for a
     * currently ongoing unlock session
     **/
    fun getTotalTimeUsed(startTimeUtc: Instant, endTimeUtc: Instant): Single<Duration> {
        if (startTimeUtc.isAfter(endTimeUtc)) throw IllegalArgumentException("startTime is after endTime")
        val completedUsageCycles = unlockStatDao
            .getUnlockStats(startTimeUtc, endTimeUtc)
            .flattenAsFlowable { it }
            .map {
                Duration.between(
                    max(startTimeUtc, it.unlockTime), min(endTimeUtc, it.lockTime!!)
                )
            }
            .reduce { val1: Duration, val2: Duration -> val1.plus(val2) }
            .defaultIfEmpty(Duration.ZERO)
            .toSingle()

        val ongoingUsage = unlockStatDao
            .getLastUnlock()
            .filter { !it.isFilled() && it.unlockTime.isBefore(endTimeUtc) }
            .map { Duration.between(it.unlockTime, endTimeUtc) }
            .defaultIfEmpty(Duration.ZERO)
            .toSingle()

        return Single.zip(completedUsageCycles, ongoingUsage, BiFunction { t1, t2 -> t1.plus(t2) })
    }

    /**
     * Defined as the first time the phone was checked after the person wakes up.
     * *I* am defining this as a time after 5am.
     * Note, here we have to convert the user's 5am to UTC because that's what things
     * are stored in.
     * @return error if the first unlock of the day hasn't happened yet
     **/
    fun getFirstUnlock(nowLocalZone: LocalDate): Single<UnlockStat> {
        val fiveAmTodayInUtc = LocalDateTime.of(nowLocalZone, LocalTime.of(5, 0))
            .toInstant(UTC)
        return unlockStatDao
            .getUnlockStats(
                fiveAmTodayInUtc,
                LocalDateTime.of(nowLocalZone, LocalTime.of(23, 59)).toInstant(UTC)
            )
            .map { it.first() }
    }

    private fun max(val1: Instant, val2: Instant): Instant {
        return if (val1.isAfter(val2)) val1 else val2
    }

    private fun min(val1: Instant, val2: Instant): Instant {
        return if (val1.isBefore(val2)) val1 else val2
    }
}