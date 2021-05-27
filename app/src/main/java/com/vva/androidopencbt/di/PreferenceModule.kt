package com.vva.androidopencbt.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.settings.PreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PreferenceModule {
    @Provides
    @Singleton
    fun providePreferenceRepository(@ApplicationContext context: Context): PreferenceRepository {
        return PreferenceRepository(PreferenceManager.getDefaultSharedPreferences(context))
    }

    @Provides
    @Singleton
    fun provideDefaultSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}