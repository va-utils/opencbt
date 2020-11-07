package com.vva.androidopencbt.db

import android.content.Context
import androidx.room.*
import com.vva.androidopencbt.R
import java.util.*

@Entity(tableName = DbContract.Diary.TABLE_NAME)
@TypeConverters(Converters::class)
data class DbRecord(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = DbContract.Diary.COLUMN_ID)
        var id: Long? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_SITUATION)
        var situation: String? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_THOUGHTS)
        var thoughts: String? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_RATIONAL)
        var rational: String? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_EMOTIONS)
        var emotions: String? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_DISTORTIONS)
        var distortions: Int? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_FEELINGS)
        var feelings: String? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_ACTIONS)
        var actions: String? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_INTENSITY)
        var intensity: Short? = null,

        @ColumnInfo(name = DbContract.Diary.COLUMN_DATETIME)
        var datetime: Date? = null
) {
    fun getDistortionsString(context: Context): String {
        val builder = StringBuilder()
        val res = context.resources
        this.distortions?.let {
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
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}