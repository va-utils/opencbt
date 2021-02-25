package com.vva.androidopencbt

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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

object DateTimeAsTimestampSerializer: KSerializer<DateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("date", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): DateTime {
        val timestamp = decoder.decodeLong()
        return DateTime(timestamp)
    }

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeLong(value.millis)
    }
}

const val DOWNLOADS_CHANNEL_ID = "com.vva.androidopencbt.downloads_channel_id"
const val DOWNLOADS_CHANNEL_TAG = 0x123

fun NotificationManager.sendDownloadNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, DOWNLOADS_CHANNEL_ID)
            .setContentTitle("Загрузка функционала")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)

    notify(DOWNLOADS_CHANNEL_TAG, builder.build())
}