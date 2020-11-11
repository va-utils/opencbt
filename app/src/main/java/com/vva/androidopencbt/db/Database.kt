package com.vva.androidopencbt.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DbRecord::class], version = DbContract.SCHEMA)
abstract class CbdDatabase: RoomDatabase() {
    abstract val databaseDao: RecordDao

    companion object {
        @Volatile
        private var INSTANCE: CbdDatabase? = null

        val MIGRATION_1_2 = object: Migration(1, 2) {
            private val oldSuffix = "_old"

            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.beginTransaction()
                    with(DbContract.Diary) {
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_SITUATION = '' WHERE $COLUMN_SITUATION IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_THOUGHTS = '' WHERE $COLUMN_THOUGHTS IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_RATIONAL = '' WHERE $COLUMN_RATIONAL IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_EMOTIONS = '' WHERE $COLUMN_EMOTIONS IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_FEELINGS = '' WHERE $COLUMN_FEELINGS IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_ACTIONS = '' WHERE $COLUMN_ACTIONS IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_INTENSITY = 0 WHERE $COLUMN_INTENSITY IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_DISTORTIONS = '' WHERE $COLUMN_DISTORTIONS IS NULL")
                        database.execSQL("UPDATE $TABLE_NAME SET " +
                                "$COLUMN_DATETIME = 0 WHERE $COLUMN_DATETIME IS NULL")
                        database.execSQL("ALTER TABLE $TABLE_NAME RENAME TO $TABLE_NAME$oldSuffix")
                        database.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME(" +
                                "$COLUMN_ID INTEGER PRIMARY KEY NOT NULL, " +
                                "$COLUMN_SITUATION TEXT NOT NULL, " +
                                "$COLUMN_THOUGHTS TEXT NOT NULL, " +
                                "$COLUMN_RATIONAL TEXT NOT NULL, " +
                                "$COLUMN_EMOTIONS TEXT NOT NULL, " +
                                "$COLUMN_FEELINGS TEXT NOT NULL, " +
                                "$COLUMN_ACTIONS TEXT NOT NULL, " +
                                "$COLUMN_INTENSITY INTEGER NOT NULL, " +
                                "$COLUMN_DISTORTIONS INTEGER NOT NULL, " +
                                "$COLUMN_DATETIME INTEGER NOT NULL)")
                        database.execSQL("INSERT INTO $TABLE_NAME " +
                                "SELECT $COLUMN_ID, " +
                                "$COLUMN_SITUATION, " +
                                "$COLUMN_THOUGHTS, " +
                                "$COLUMN_RATIONAL, " +
                                "$COLUMN_EMOTIONS, " +
                                "$COLUMN_FEELINGS, " +
                                "$COLUMN_ACTIONS, " +
                                "$COLUMN_INTENSITY, " +
                                "$COLUMN_DISTORTIONS, " +
                                "$COLUMN_DATETIME FROM $TABLE_NAME$oldSuffix")
                        database.setTransactionSuccessful()
                    }
                } catch (e: Exception) {
                } finally {
                    database.endTransaction()
                }
            }
        }

        fun getInstance(context: Context): CbdDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            CbdDatabase::class.java,
                            DbContract.DATABASE_NAME
                    )
                            .addMigrations(MIGRATION_1_2)
                            .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}