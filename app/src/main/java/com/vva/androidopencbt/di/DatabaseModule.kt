package com.vva.androidopencbt.di

import android.content.Context
import androidx.room.Room
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.RecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CbdDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            CbdDatabase::class.java,
            DbContract.DATABASE_NAME
        )
            .addMigrations(CbdDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideDao(database: CbdDatabase): RecordDao {
        return database.databaseDao
    }
}