package com.crearo.halt

import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.toReadableString(): String =
    this.truncatedTo(ChronoUnit.MILLIS).toString().replace("[TZ]".toRegex(), " ")
