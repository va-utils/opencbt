package com.vva.androidopencbt.db

object DbContract {
    const val DATABASE_NAME = "opencbd_beta9.db" // название бд
    const val SCHEMA = 1 // версия базы данных
    const val ORDER_ASC = 0
    const val ORDER_DESC = 1

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
    }
}



