package com.vva.androidopencbt.db

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room

@Database(entities = [DbRecord::class], version = DbContract.SCHEMA)
abstract class CbdDatabase: RoomDatabase() {
    abstract val databaseDao: RecordDao

    companion object {
        @Volatile
        private var INSTANCE: CbdDatabase? = null

        fun getInstance(context: Context): CbdDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            CbdDatabase::class.java,
                            DbContract.DATABASE_NAME
                    ).build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}