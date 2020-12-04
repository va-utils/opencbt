package com.vva.androidopencbt.db

import android.content.Context
import androidx.room.*
import com.vva.androidopencbt.DateTimeAsTimestampSerializer
import com.vva.androidopencbt.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.joda.time.DateTime

@Serializable
@Entity(tableName = DbContract.Diary.TABLE_NAME)
@TypeConverters(Converters::class)
data class DbRecord(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = DbContract.Diary.COLUMN_ID)
        @Transient
        var id: Long = 0L,

        @ColumnInfo(name = DbContract.Diary.COLUMN_SITUATION)
        var situation: String = "",

        @ColumnInfo(name = DbContract.Diary.COLUMN_THOUGHTS)
        var thoughts: String = "",

        @ColumnInfo(name = DbContract.Diary.COLUMN_RATIONAL)
        var rational: String = "",

        @ColumnInfo(name = DbContract.Diary.COLUMN_EMOTIONS)
        var emotions: String = "",

        @ColumnInfo(name = DbContract.Diary.COLUMN_DISTORTIONS)
        var distortions: Int = 0x0,

        @ColumnInfo(name = DbContract.Diary.COLUMN_FEELINGS)
        var feelings: String = "",

        @ColumnInfo(name = DbContract.Diary.COLUMN_ACTIONS)
        var actions: String = "",

        @ColumnInfo(name = DbContract.Diary.COLUMN_INTENSITY)
        var intensity: Int = 0,

        @ColumnInfo(name = DbContract.Diary.COLUMN_DATETIME)
        @Serializable(with = DateTimeAsTimestampSerializer::class)
        var datetime: DateTime = DateTime()
) {
    fun getDistortionsString(context: Context): String {
        val builder = StringBuilder()
        val res = context.resources
        this.distortions.let {
            if (it.and(ALL_OR_NOTHING) != 0) builder.append(res.getString(R.string.dist_all_or_nothing)).append(", ")
            if (it.and(OVERGENERALIZING) != 0) builder.append(res.getString(R.string.dist_overgeneralizing)).append(", ")
            if (it.and(FILTERING) != 0) builder.append(res.getString(R.string.dist_filtering)).append(", ")
            if (it.and(DISQUAL_POSITIVE) != 0) builder.append(res.getString(R.string.dist_disqual_positive)).append(", ")
            if (it.and(JUMP_CONCLUSION) != 0) builder.append(res.getString(R.string.dist_jump_conclusion)).append(", ")
            if (it.and(MAGN_AND_MIN) != 0) builder.append(res.getString(R.string.dist_magn_and_min)).append(", ")
            if (it.and(EMOTIONAL_REASONING) != 0) builder.append(res.getString(R.string.dist_emotional_reasoning)).append(", ")
            if (it.and(MUST_STATEMENTS) != 0) builder.append(res.getString(R.string.dist_must_statement)).append(", ")
            if (it.and(LABELING) != 0) builder.append(res.getString(R.string.dist_labeling)).append(", ")
            if (it.and(PERSONALIZATION) != 0) builder.append(res.getString(R.string.dist_personalistion)).append(", ")
        }

        return if (builder.length > 2) {
            builder.substring(0, builder.length - 2).toString()
        } else {
            ""
        }
    }

    companion object {
        //---список когнитивных искажений
        const val ALL_OR_NOTHING: Int = 0x1
        const val OVERGENERALIZING: Int = 0x2
        const val FILTERING: Int = 0x4
        const val DISQUAL_POSITIVE: Int = 0x8
        const val JUMP_CONCLUSION: Int = 0x10
        const val MAGN_AND_MIN: Int = 0x20
        const val EMOTIONAL_REASONING: Int = 0x40
        const val MUST_STATEMENTS: Int = 0x80
        const val LABELING: Int = 0x100
        const val PERSONALIZATION: Int = 0x200
    }

    fun equalsIgnoreId(record: DbRecord): Boolean {
        if (this === record) return true

        if (situation != record.situation) return false
        if (thoughts != record.thoughts) return false
        if (rational != record.rational) return false
        if (emotions != record.emotions) return false
        if (distortions != record.distortions) return false
        if (feelings != record.feelings) return false
        if (actions != record.actions) return false
        if (intensity != record.intensity) return false
        if (datetime != record.datetime) return false

        return true
    }
}

@Suppress("unused")
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): DateTime {
        return DateTime(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: DateTime): Long {
        return date.millis
    }

    @TypeConverter
    fun dateTimeToSting(date: DateTime): String {
        return date.toString()
    }

    @TypeConverter
    fun stringToDateTime(string: String): DateTime {
        return DateTime(string)
    }
}