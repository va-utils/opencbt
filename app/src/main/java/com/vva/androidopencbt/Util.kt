package com.vva.androidopencbt

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

const val FORMAT_DATE_TIME = "HH:mm dd MMMM yyyy"
const val FORMAT_DATE = "dd/MM/yyyy"
const val FORMAT_DATE_TIME_FOR_STATS = "dd MMMM yyyy HH:mm"

fun DateTime.getDateTimeString(): String {
    return DateTimeFormat.forPattern(FORMAT_DATE_TIME).print(this)
}

fun DateTime.getShortDateTime(): String {
    return DateTimeFormat.forPattern(FORMAT_DATE_TIME).print(this)
}

fun DateTime.getStatsDateTime(): String {
    return DateTimeFormat.forPattern(FORMAT_DATE_TIME_FOR_STATS).print(this)
}

fun DateTime.getDateString(): String {
    return DateTimeFormat.forPattern(FORMAT_DATE).print(this)
}

fun DateTime.beginOfMonth() = DateTime(this.year, this.monthOfYear, 1, 0, 0)

fun DateTime.endOfDay() = DateTime(this.year, this.monthOfYear, this.dayOfMonth, 23, 59)
