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

    private val _isIntensityColorEnabled = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_INTENSITY_COLOR, false)
    }
    val isIntensityColorEnabled: LiveData<Boolean>
        get() = _isIntensityColorEnabled

    //PREFERENCE_INTENSITY_COLOR
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

    private val _isDividersEnabled = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_DIVIDERS_ENABLED,true)
    }
    val isDividersEnabled : LiveData<Boolean>
        get() = _isDividersEnabled

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

    private val _isSuggestEnabled = MutableLiveData<Boolean>().apply {
        value = sharedPreferences.getBoolean(PREFERENCE_SUGGEST_ENABLED, false)
    }
    val isSuggestEnabled: LiveData<Boolean>
        get() = _isSuggestEnabled

    private val _isDriveIntegrationEnabled = MutableLiveData(sharedPreferences.getBoolean(PREFERENCE_GDRIVE_ENABLED, false))
    val isDriveIntegrationEnabled: LiveData<Boolean>
        get() = _isDriveIntegrationEnabled

    private val _defaultExportFormat = MutableLiveData<ExportFormats>().apply {
        value = when (sharedPreferences.getString(PREFERENCE_DEFAULT_EXPORT, ExportFormats.JSON.formatString)) {
            ExportFormats.JSON.formatString -> {
                ExportFormats.JSON
            }
            ExportFormats.HTML.formatString -> {
                ExportFormats.HTML
            }
            ExportFormats.CSV.formatString -> {
                ExportFormats.CSV
            }
            else -> {
                throw IllegalStateException("No such format")
            }
        }
    }
    val defaultExportFormat: LiveData<ExportFormats>
        get() = _defaultExportFormat

    private val _isScreenSecureEnabled = MutableLiveData(sharedPreferences.getBoolean(
        PREFERENCE_SCREEN_SECURE, false))
    val isScreenSecureEnabled: LiveData<Boolean>
        get() = _isScreenSecureEnabled

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

                    PREFERENCE_INTENSITY_COLOR ->
                    {
                        _isIntensityColorEnabled.value = sharedPreferences.getBoolean(PREFERENCE_INTENSITY_COLOR, false)
                    }

                    PREFERENCE_SUGGEST_ENABLED-> {
                        _isSuggestEnabled.value = sharedPreferences.getBoolean(PREFERENCE_SUGGEST_ENABLED, false)
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
                        _defaultExportFormat.value = when (sharedPreferences.getString(PREFERENCE_DEFAULT_EXPORT, ExportFormats.JSON.formatString)) {
                            ExportFormats.JSON.formatString -> {
                                ExportFormats.JSON
                            }
                            ExportFormats.HTML.formatString -> {
                                ExportFormats.HTML
                            }
                            ExportFormats.CSV.formatString -> {
                                ExportFormats.CSV
                            }
                            else -> {
                                throw IllegalStateException("No such format")
                            }
                        }
                    }
                    PREFERENCE_DIVIDERS_ENABLED -> {
                        _isDividersEnabled.value = sharedPreferences.getBoolean(PREFERENCE_DIVIDERS_ENABLED, true)
                    }
                    PREFERENCE_GDRIVE_ENABLED -> {
                        _isDriveIntegrationEnabled.value = sharedPreferences.getBoolean(PREFERENCE_GDRIVE_ENABLED, false)
                    }
                    PREFERENCE_SCREEN_SECURE -> {
                        _isScreenSecureEnabled.value = sharedPreferences.getBoolean(
                            PREFERENCE_SCREEN_SECURE, false)


                    }
                }
            }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    companion object {
        const val PREFERENCE_NIGHT_MODE = "enable_night_theme"
        const val PREFERENCE_INTENSITY_COLOR = "enable_color_intesity"
        const val PREFERENCE_INTENSITY_INDICATION = "enable_intensity_indication"
        const val PREFERENCE_QUOTES_ENABLED = "enable_quotes"
        const val PREFERENCE_IS_DESC_ORDER = "desc_ordering"
        const val PREFERENCE_ENABLE_PIN = "enable_pin_protection"
        const val PREFERENCE_DEFAULT_EXPORT = "default_export"
        const val PREFERENCE_DIVIDERS_ENABLED = "enable_dividers"
        const val PREFERENCE_GDRIVE_ENABLED = "enable_gdrive_integration"
        const val PREFERENCE_GDRIVE_EXPORT = "setting_export_gdrive"
        const val PREFERENCE_GDRIVE_IMPORT = "setting_import_gdrive"
        const val PREFERENCE_LOCAL_EXPORT = "setting_export_local"
        const val PREFERENCE_LOCAL_IMPORT = "setting_import_local"
        const val PREFERENCE_ABOUT = "setting_about"
        const val PREFERENCE_SCREEN_SECURE = "enable_flag_screen_secure"
        const val PREFERENCE_SUGGEST_ENABLED = "enable_suggest"
    }
}