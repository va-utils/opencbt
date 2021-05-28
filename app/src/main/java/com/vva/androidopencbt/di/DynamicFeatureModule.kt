package com.vva.androidopencbt.di

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DynamicFeatureModule {
    @Provides
    @Singleton
    fun provideSplitInstallManager(@ApplicationContext context: Context): SplitInstallManager {
        return SplitInstallManagerFactory.create(context)
    }
}