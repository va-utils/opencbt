package com.vva.androidopencbt.db

@Suppress("unused")
object DbContract {
    const val DATABASE_NAME = "opencbd_beta9.db" // название бд
    const val SCHEMA = 2 // версия базы данных
    const val ORDER_DESC = 0
    const val ORDER_ASC = 1

    object Diary {
        const val TABLE_NAME = "diary" // название таблицы в бд

        const val COLUMN_ID = "_id"
        const val COLUMN_SITUATION = "_situation"
        const val COLUMN_THOUGHTS = "_thoughts"
        const val COLUMN_RATIONAL = "_rational"
        const val COLUMN_EMOTIONS = "_emotions"
        const val COLUMN_DISTORTIONS = "_distortions"
        const val COLUMN_FEELINGS = "_feelings"
        const val COLUMN_ACTIONS = "_actions"
        const val COLUMN_INTENSITY = "_intensity"
        const val COLUMN_DATETIME = "_datetime"

        const val GET_ALL_SQL = ("SELECT * FROM $TABLE_NAME")
        const val GET_RECORD_BY_ID = ("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
        const val GET_ALL_SQL_ORDERED = ("SELECT * FROM $TABLE_NAME ORDER BY CASE WHEN :order = 0 THEN $COLUMN_DATETIME END DESC, CASE WHEN :order = 1 THEN $COLUMN_DATETIME END ASC")

        //---statistic

        const val GET_ALL_COUNT = ("SELECT COUNT(*) FROM $TABLE_NAME")
        const val GET_AVERAGE_INTENSITY = ("SELECT avg($COLUMN_INTENSITY) FROM $TABLE_NAME")
        const val GET_MIN_DATE = ("SELECT MIN($COLUMN_DATETIME) FROM $TABLE_NAME")
        const val GET_MAX_DATE = ("SELECT MAX($COLUMN_DATETIME) FROM $TABLE_NAME")
        const val GET_DISTORTION_COL = ("SELECT $COLUMN_DISTORTIONS FROM $TABLE_NAME")
        const val GET_DATETIME_COL = ("SELECT $COLUMN_DATETIME FROM $TABLE_NAME")

        //---send diary
//        const val GET_RECORDS_FOR_PERIOD = ("SELECT * FROM $TABLE_NAME WHERE $COLUMN_DATETIME >= :t1 AND $COLUMN_DATETIME <= :t2")
        const val GET_RECORDS_FOR_PERIOD = ("SELECT * FROM $TABLE_NAME WHERE $COLUMN_DATETIME BETWEEN :t1 AND :t2")
    }
}



