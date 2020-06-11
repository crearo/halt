package com.crearo.halt.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The idea is that each unlock corresponds to a lock time. This makes querying for data much easier.
 * Haven't considered any alternatives.
 * At the moment, just storing this and being able to query seems like it's good enough.
 **/
@Entity(tableName = "unlock_stats_table")
data class UnlockStat(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "unlock_time") val unlockTime: Long,
    @ColumnInfo(name = "lock_time") val lockTime: Long
)