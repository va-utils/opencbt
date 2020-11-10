package com.vva.androidopencbt

import java.text.SimpleDateFormat
import java.util.*

fun DateTime.getDateTimeString(): String {
    return DateTimeFormat.forPattern("HH:mm dd MMMM yyyy").print(this)
}

fun DateTime.getShortDateTime(): String {
    return DateTimeFormat.forPattern("HH:mm dd/MM/yyyy").print(this)
}

fun DateTime.getDateString(): String {
    return DateTimeFormat.forPattern("dd/MM/yyyy").print(this)
}

fun DateTime.beginOfMonth() = DateTime(this.year, this.monthOfYear, 1, 0, 0)

fun DateTime.endOfDay() = DateTime(this.year, this.monthOfYear, this.dayOfMonth, 23, 59)
