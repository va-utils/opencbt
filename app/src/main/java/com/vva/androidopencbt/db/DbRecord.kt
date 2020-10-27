package com.vva.androidopencbt.db

import androidx.room.*
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