package com.vva.androidopencbt.settings

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PreferenceRepository(private val sharedPreferences: SharedPreferences) {
    private val _isNightThemeLive = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_NIGHT_MODE, false)
    }
    val isNightThemeLive: LiveData<Boolean>
        get() = _isNightThemeLive

    private val preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                _, key ->
                when (key) {
                    PREFERENCE_NIGHT_MODE -> {
                        _isNightThemeLive.value = sharedPreferences.getBoolean(PREFERENCE_NIGHT_MODE, false)
                    }
                }
            }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    companion object {
        private const val PREFERENCE_NIGHT_MODE = "enable_night_theme"
    }
}