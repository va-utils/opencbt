package com.vva.androidopencbt

import java.text.SimpleDateFormat
import java.util.*

fun Date.getDateTimeString(): String {
    val formatter = SimpleDateFormat("HH:mm dd MMMM yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun Date.getShortDateTime(): String {
    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun Date.getDateString(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun Date.beginOfMonth(): Date
{
    val d : Date = Date(this.year, this.month, 1)
    return d
}

fun Date.endOfDay() : Date
{
    val d = Date(this.year, this.month, this.date)
    d.hours=23
    d.minutes=59
    d.seconds=59
    return d
}
