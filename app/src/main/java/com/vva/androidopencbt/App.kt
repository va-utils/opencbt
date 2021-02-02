package com.vva.androidopencbt

import android.app.Application
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.settings.PreferenceRepository

class App: Application() {
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        preferenceRepository = PreferenceRepository(
                PreferenceManager.getDefaultSharedPreferences(this)
        )
    }
}