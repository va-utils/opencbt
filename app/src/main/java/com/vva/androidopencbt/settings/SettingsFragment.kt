package com.vva.androidopencbt.settings

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.*
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

private const val REQUEST_CODE_KG_PROTECTION = 0x99

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
        arrayOf(
                findPreference<Preference>("enable_thoughts") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_rational") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_situation") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_emotions") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_intensity") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_feelings") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_actions") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_distortions") as SwitchPreferenceCompat
        ).forEach {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
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
        }

        (findPreference<Preference>("desc_ordering") as SwitchPreferenceCompat).setOnPreferenceChangeListener {
            _, newValue ->
            viewModel.setOrder(newValue as Boolean)

            true
        }

        (findPreference<Preference>("enable_quotes") as SwitchPreferenceCompat).setOnPreferenceChangeListener {
            _, newValue ->
            viewModel.setQuotes(newValue as Boolean)

            true
        }

        findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.setOnPreferenceChangeListener {
            _, _ ->
            val km = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i = km.createConfirmDeviceCredentialIntent(null, null)
                startActivityForResult(i, REQUEST_CODE_KG_PROTECTION)
            }
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_KG_PROTECTION -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.let {
                        it.isChecked = !it.isChecked
                    }
                }
            }
        }
    }
}