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
     * 1. We aren't inserting a new unlock before inserting the previous lock
     * 2. The insert is sequential and monotonically increasing
     *
     * It also tries to recover from an unusual state where we have two consecutive unlocks without
     * a lock. mark the previous record so that the user spent 0 seconds on it, and log this on,
     * todo: and propagate this as an error. Also log it to firebase.
     **/
    fun addNewUnlock(unlockInstantUtc: Instant): Completable {
        val insertNewUnlockCompletable = unlockStatDao.insertNewUnlock(UnlockStat(unlockInstantUtc))

        val checkPreviousRecordFilledCompletable = unlockStatDao
            .getLastUnlock()
            .onErrorReturnItem(UnlockStat.EMPTY)
            .flatMapCompletable {
                when {
                    !it.isFilled() -> {
                        addNewLock(it.unlockTime)
                        // todo: throw IllegalStateException("Recovering from a situation where previous record was not filled.")
                        //  I'm unable to get this to throw over here
                    }
                    it.lockTime!!.isAfter(unlockInstantUtc) -> {
                        Completable.error(
                            IllegalStateException(
                                "Previous lock time is after currently unlock time. Previous Lock Time= ${it.lockTime}, Current Unlock Time= $unlockInstantUtc"
                            )
                        )
                    }
                    else -> {
                        Completable.complete()
                    }
                }
            }

        return checkPreviousRecordFilledCompletable.andThen(insertNewUnlockCompletable)
    }

    fun addNewLock(lockInstantUtc: Instant): Completable {
        return unlockStatDao
            .getLastUnlock()
            .flatMapCompletable {
                when {
                    it.isFilled() -> {
                        Completable.error { IllegalStateException("Previous record already recorded a lock.") }
                    }
                    it.unlockTime.isAfter(lockInstantUtc) -> {
                        Completable.error {
                            IllegalStateException(
                                "Previous unlock time is after current lock time." + "Previous Unlock Time= ${it.unlockTime}, Current Lock Time= $lockInstantUtc"
                            )
                        }
                    }
                    else -> {
                        it.lockTime = lockInstantUtc
                        unlockStatDao.updateCorrespondingLock(it)
                    }
                }
            }
    }

    fun getUnlockStats(): Flowable<List<UnlockStat>> {
        return unlockStatDao.getUnlockStats()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    fun getLastUnlock(): Single<UnlockStat> {
        return unlockStatDao.getLastUnlock()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    /**
     * @return total time the phone is used between the given parameters.
     **/
    fun getTotalTimeUsed(
        startInstant: Instant,
        endInstant: Instant
    ): Single<Duration> {
        if (startInstant.isAfter(endInstant)) throw IllegalArgumentException("startTime is after endTime")
        return unlockStatDao
            .getUnlockStats(startInstant, endInstant)
            .flattenAsFlowable { it }
            .map {
                Duration.between(
                    max(startInstant, it.unlockTime), min(endInstant, it.lockTime!!)
                )
            }
            .reduce { val1: Duration, val2: Duration -> val1.plus(val2) }
            .defaultIfEmpty(Duration.ZERO)
            .toSingle()
    }

    /**
     * You would typically want this when querying for total time used until now(), where the phone
     * is also unlocked
     * @param startInstant note, an instant is always in UTC
     * @return total time the phone is used between the given parameters. This accounts for a
     * currently ongoing unlock session
     **/
    fun getTotalTimeUsedWithOngoingSession(
        startInstant: Instant,
        endInstant: Instant
    ): Single<Duration> {
        if (startInstant.isAfter(endInstant)) throw IllegalArgumentException("startTime is after endTime")
        val ongoingUsage = unlockStatDao
            .getLastUnlock()
            .filter {
                !it.isFilled()
                        && it.unlockTime.isBefore(endInstant)
                        && it.unlockTime.isAfter(startInstant)
            }
            .map { Duration.between(it.unlockTime, endInstant) }
            .defaultIfEmpty(Duration.ZERO)
            .toSingle()

        return Single.zip(
            getTotalTimeUsed(startInstant, endInstant),
            ongoingUsage,
            BiFunction { t1, t2 -> t1.plus(t2) })
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