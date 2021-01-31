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

    private val _isIntensityIndicationEnabled = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_INTENSITY_INDICATION, false)
    }
    val isIntensityIndicationEnabled: LiveData<Boolean>
        get() = _isIntensityIndicationEnabled

    private val _isQuotesEnabled = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_QUOTES_ENABLED, false)
    }
    val isQuotesEnabled: LiveData<Boolean>
        get() = _isQuotesEnabled

    private val _isDescOrder = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_IS_DESC_ORDER, false)
    }
    val isDescOrder: LiveData<Boolean>
        get() = _isDescOrder

    private val _isPinEnabled = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_ENABLE_PIN, false)
    }
    val isPinEnabled: LiveData<Boolean>
        get() = _isPinEnabled

    private val _defaultExportFormat = MutableLiveData<String>().apply {
        value = sharedPreferences.getString(PREFERENCE_DEFAULT_EXPORT, "JSON")
    }
    val defaultExportFormat: LiveData<String>
        get() = _defaultExportFormat

    private val preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                _, key ->
                when (key) {
                    PREFERENCE_NIGHT_MODE -> {
                        _isNightThemeLive.value = sharedPreferences.getBoolean(PREFERENCE_NIGHT_MODE, false)
                    }
                    PREFERENCE_INTENSITY_INDICATION -> {
                        _isIntensityIndicationEnabled.value = sharedPreferences.getBoolean(PREFERENCE_INTENSITY_INDICATION, false)
                    }
                    PREFERENCE_QUOTES_ENABLED -> {
                        _isQuotesEnabled.value = sharedPreferences.getBoolean(PREFERENCE_QUOTES_ENABLED, false)
                    }
                    PREFERENCE_IS_DESC_ORDER -> {
                        _isDescOrder.value = sharedPreferences.getBoolean(PREFERENCE_IS_DESC_ORDER, false)
                    }
                    PREFERENCE_ENABLE_PIN -> {
                        _isPinEnabled.value = sharedPreferences.getBoolean(PREFERENCE_ENABLE_PIN, false)
                    }
                    PREFERENCE_DEFAULT_EXPORT -> {
                        _defaultExportFormat.value = sharedPreferences.getString(PREFERENCE_DEFAULT_EXPORT, "JSON")
                    }
                }
            }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    companion object {
        private const val PREFERENCE_NIGHT_MODE = "enable_night_theme"
        private const val PREFERENCE_INTENSITY_INDICATION = "enable_intensity_indication"
        private const val PREFERENCE_QUOTES_ENABLED = "enable_quotes"
        private const val PREFERENCE_IS_DESC_ORDER = "desc_ordering"
        private const val PREFERENCE_ENABLE_PIN = "enable_pin_protection"
        private const val PREFERENCE_DEFAULT_EXPORT = "default_export"
    }
}