package com.crearo.halt.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * The idea is that each unlock corresponds to a lock time. This makes querying for data much easier.
 * Haven't considered any alternatives.
 * At the moment, just storing this and being able to query seems like it's good enough.
 **/
@Entity(tableName = "unlock_stats_table")
data class UnlockStat(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "unlock_time") val unlockTime: Instant,
    @ColumnInfo(name = "lock_time") var lockTime: Instant? = null

) {
    constructor(unlockTime: Instant, lockTime: Instant? = null) :
            this(0, unlockTime, lockTime)

    /** @return if the both the unlock and lock time have been filled */
    fun isFilled(): Boolean {
        return unlockTime.epochSecond != -1L && lockTime != null && lockTime!!.epochSecond != -1L
    }

    companion object {
        val EMPTY = UnlockStat(0, Instant.ofEpochSecond(0), Instant.ofEpochSecond(0))
    }
}