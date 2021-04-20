package com.vva.androidopencbt

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.google.android.play.core.splitcompat.SplitCompat
import com.vva.androidopencbt.settings.PreferenceRepository

class App: Application() {
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        preferenceRepository = PreferenceRepository(
                PreferenceManager.getDefaultSharedPreferences(this)
        )
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }
}