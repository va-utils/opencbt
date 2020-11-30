package com.vva.androidopencbt.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.amirarcane.lockscreen.activity.EnterPinActivity
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.LockManager
import com.vva.androidopencbt.PinActivity
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

private const val REQUEST_CODE_LOLLIPIN_ENABLE = 0x101
private const val REQUEST_CODE_LOLLIPIN_DISABLE = 0x102

class SettingsFragmentRoot: Fragment() {
    private lateinit var linearLayout: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.settings_toolbar).setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        linearLayout = inflater.inflate(R.layout.fragment_settings, container, false) as LinearLayout

        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragmentNew())
                .commit()

        return linearLayout
    }
}

class SettingsFragmentNew : PreferenceFragmentCompat() {
    private lateinit var prefs: Array<SwitchPreferenceCompat>
    private val viewModel: RecordsViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        prefs = arrayOf(
                findPreference<Preference>("enable_thoughts") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_rational") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_situation") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_emotions") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_intensity") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_feelings") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_actions") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_distortions") as SwitchPreferenceCompat
        )
        for (i in 0..7) {
            prefs[i].onPreferenceClickListener = lsnr
        }

        (findPreference<Preference>("desc_ordering") as SwitchPreferenceCompat).setOnPreferenceChangeListener {
            preference, newValue ->
            (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
            viewModel.setOrder(newValue)

            newValue
        }

        (findPreference<Preference>("enable_quotes") as SwitchPreferenceCompat).setOnPreferenceChangeListener {
            preference, newValue ->
            (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
            viewModel.setQuotes(newValue)

            newValue
        }

        (findPreference<ListPreference>("default_export") as ListPreference).setOnPreferenceChangeListener {
            preference, newValue ->

            true
        }

        findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.setOnPreferenceChangeListener {
            p, newValue ->
            if (newValue as Boolean) {
//                val intent: Intent = EnterPinActivity.getIntent(requireContext(), true)
//                startActivityForResult(intent, 0x100)
                val intent = Intent(requireContext(), PinActivity::class.java)
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
                startActivityForResult(intent, REQUEST_CODE_LOLLIPIN_ENABLE)
            } else {
//                val intent = Intent(requireContext(), EnterPinActivity::class.java)
//                startActivityForResult(intent, 0x99)
                val intent = Intent(requireContext(), PinActivity::class.java)
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK)
                startActivityForResult(intent, REQUEST_CODE_LOLLIPIN_DISABLE)
//                (p as SwitchPreferenceCompat).isChecked = false
//                val lockM = LockManager.getInstance()
//                lockM.disableAppLock()
            }
            newValue
        }
    }

    var lsnr = Preference.OnPreferenceClickListener { preference ->
        var flag = false
        for (i in 0..7) {
            if (prefs[i].isChecked) {
                flag = true
                break
            }
        }
        if (!flag) {
            Toast.makeText(context, getString(R.string.pref_empty), Toast.LENGTH_SHORT).show()
            (preference as SwitchPreferenceCompat).isChecked = true
        }

        true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0x99 -> {
                findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.let {
                    if (resultCode == EnterPinActivity.RESULT_BACK_PRESSED) {
                        it.isChecked = true
                    } else if (resultCode == EnterPinActivity.RESULT_OK) {
                        it.isChecked = false
                    }
                }
            }
            0x100 -> {
                findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.let {
                    if (resultCode == EnterPinActivity.RESULT_OK) {
                        it.isChecked = true
                    }
                    if (resultCode == EnterPinActivity.RESULT_BACK_PRESSED) {
                        it.isChecked = false
                    }
                }
            }
            REQUEST_CODE_LOLLIPIN_ENABLE -> {
                findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.let {
                    Log.d("LOLLI", resultCode.toString())
                    if (resultCode == -1)
                        it.isChecked = true

                }
            }
            REQUEST_CODE_LOLLIPIN_DISABLE -> {
                Log.d("LOLLI", resultCode.toString())
                findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.let {
                    if (resultCode == -1)
                        it.isChecked = false
                }
            }
        }
    }
}