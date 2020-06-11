package com.crearo.halt.data

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.time.Instant

/**
 * The assumption here is every unlock has a corresponding lock. The time between these two events
 * is how much you've used your phone.
 *
 * Todo Queries:
 * - get yesterday's last unlock (how do you define yesterday? This isn't a straightforward query
 *      for the last entry before 12am. I sleep after that, and so my last use for that day is when
 *      there is a gap of more than say 2-3 hours of use)
 * - get today's first unlock (again, if I can solve the above, this is straighforward. Actually,
 *      I could get this a little better. First unlock is the first usage after 3am, given that I
 *      don't check my phone for 2-3 hours before that. It's still an interesting problem to solve.
 *      Mostly for the edge cases. I should just write it down.)
 **/
@Dao
interface UnlockStatDao {

    /**
     * This should also ensure that a new unlock can only be inserted if the previous row has a
     * corresponding lock. If not, we in deep shit.
     **/
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertNewUnlock(unlockStat: UnlockStat): Completable

    @Update
    fun updateCorrespondingLock(unlockStat: UnlockStat): Completable

    @Query("DELETE FROM unlock_stats_table")
    fun deleteAll(): Completable

    @Query("SELECT * FROM unlock_stats_table ORDER BY unlock_time LIMIT 1") // todo where lock_time is empty
    fun getLastUnlock(): Single<UnlockStat>

    /*** todo ensure startTime < endTime*/
    @Query("SELECT * FROM unlock_stats_table WHERE lock_time >= :startTime AND unlock_time <= :endTime")
    fun getUnlockStats(startTime: Instant, endTime: Instant): Single<List<UnlockStat>>

    @Query("SELECT * FROM unlock_stats_table")
    fun getUnlockStats(): Flowable<List<UnlockStat>>

}