package com.vva.androidopencbt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import androidx.databinding.BindingAdapter
import com.vva.androidopencbt.db.DbRecord
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
const val FORMAT_DATE_TIME_DRIVE = "dd MMMM yyyy HH:mm"
const val FORMAT_DATE_HEADER = "dd MMMM yyyy"

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

fun DateTime.getDateHeaderString(): String {
    return DateTimeFormat.forPattern(FORMAT_DATE_HEADER).print(this)
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

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
        @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
            intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

@BindingAdapter("setDistortionsString")
fun TextView.setDistortionsString(distortions: Int) {
    if (distortions == 0x0)
        return

    val builder = StringBuilder()
    with(distortions) {
        if (this.and(DbRecord.ALL_OR_NOTHING) != 0) builder.append(context.resources.getString(R.string.dist_all_or_nothing)).append(", ")
        if (this.and(DbRecord.OVERGENERALIZING) != 0) builder.append(context.resources.getString(R.string.dist_overgeneralizing)).append(", ")
        if (this.and(DbRecord.FILTERING) != 0) builder.append(context.resources.getString(R.string.dist_filtering)).append(", ")
        if (this.and(DbRecord.DISQUAL_POSITIVE) != 0) builder.append(context.resources.getString(R.string.dist_disqual_positive)).append(", ")
        if (this.and(DbRecord.JUMP_CONCLUSION) != 0) builder.append(context.resources.getString(R.string.dist_jump_conclusion)).append(", ")
        if (this.and(DbRecord.MAGN_AND_MIN) != 0) builder.append(context.resources.getString(R.string.dist_magn_and_min)).append(", ")
        if (this.and(DbRecord.EMOTIONAL_REASONING) != 0) builder.append(context.resources.getString(R.string.dist_emotional_reasoning)).append(", ")
        if (this.and(DbRecord.MUST_STATEMENTS) != 0) builder.append(context.resources.getString(R.string.dist_must_statement)).append(", ")
        if (this.and(DbRecord.LABELING) != 0) builder.append(context.resources.getString(R.string.dist_labeling)).append(", ")
        if (this.and(DbRecord.PERSONALIZATION) != 0) builder.append(context.resources.getString(R.string.dist_personalistion)).append(", ")
    }
    this.text = builder.substring(0, builder.length - 2).toString()
}