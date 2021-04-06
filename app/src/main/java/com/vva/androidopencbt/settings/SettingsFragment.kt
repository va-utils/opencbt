package com.vva.androidopencbt.settings

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.vva.androidopencbt.App
import com.vva.androidopencbt.R
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.export.Export
import com.vva.androidopencbt.export.ImportViewModel
import com.vva.androidopencbt.export.ImportViewModelFactory
import com.vva.androidopencbt.export.ProcessStates
import com.vva.androidopencbt.playfeatures.FeatureDownloadViewModel
import com.vva.androidopencbt.settings.widgets.SwitchProgressPreference

const val GDRIVE_MODULE_NAME = "gdrive_backup_feature"

class SettingsFragmentRoot: Fragment() {
    private lateinit var linearLayout: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.settings_toolbar).setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        linearLayout = inflater.inflate(R.layout.fragment_settings, container, false) as LinearLayout

        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragmentNew())
                .commit()

        return linearLayout
    }
}

class SettingsFragmentNew : PreferenceFragmentCompat() {
    private val logTag = javaClass.canonicalName
    private lateinit var prefs: Array<SwitchPreferenceCompat>
    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var manager: SplitInstallManager
    private lateinit var dao: RecordDao
    private val importViewModel: ImportViewModel by viewModels {
        ImportViewModelFactory(dao)
    }
    private val featureDownloadViewModel: FeatureDownloadViewModel by activityViewModels()
    private lateinit var driveEnabled: SwitchProgressPreference

    private val backupPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it?.data?.data?.let { uri ->
            requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            importViewModel.importRecordsFromFile(uri, requireContext())
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceRepository = (requireActivity().application as App).preferenceRepository
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        dao = CbdDatabase.getInstance(requireContext()).databaseDao

        manager = SplitInstallManagerFactory.create(requireContext())

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
        prefs.onEach {
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

        val pendingActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == AppCompatActivity.RESULT_OK) {
                findPreference<SwitchPreferenceCompat>("enable_pin_protection")?.let {
                    it.isChecked = !it.isChecked
                }
            }
        }

        findPreference<SwitchPreferenceCompat>(PreferenceRepository.PREFERENCE_ENABLE_PIN)?.setOnPreferenceChangeListener {
            _, _ ->
            val km = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i = km.createConfirmDeviceCredentialIntent(null, null)

                pendingActivity.launch(i)
            } else {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setMessage(getString(R.string.pref_pin_problem))
                    setTitle(getString(R.string.pref_pin_title))
                    setPositiveButton("OK") { _, _ -> startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))}
                    setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel()}
                }.create().show()
            }
            false
        }

        findPreference<Preference>(PreferenceRepository.PREFERENCE_ABOUT)?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragmentRoot_to_aboutFragment)
            true
        }

        initDriveImExportListeners()
        initLocalImExportListeners()
    }

    private fun initDriveImExportListeners() {
        driveEnabled = findPreference<SwitchProgressPreference>(PreferenceRepository.PREFERENCE_GDRIVE_ENABLED) as SwitchProgressPreference
        driveEnabled.setOnPreferenceChangeListener { preference, newValue ->
//            if (!manager.installedModules.contains(GDRIVE_MODULE_NAME) && newValue == true) {
            if (newValue == true) {
//                        DriveDownloader(requireContext(), preference).download()
                featureDownloadViewModel.driveFeatureDownload()
                false
            } else {
                true
            }
        }

        driveEnabled.setOnCancelClickListener {
            featureDownloadViewModel.driveInstallCancel()
        }

        findPreference<Preference>(PreferenceRepository.PREFERENCE_GDRIVE_IMPORT)?.setOnPreferenceClickListener {
            if (!manager.installedModules.contains(GDRIVE_MODULE_NAME)) {
                Toast.makeText(requireContext(), "Модуль еще не установлен", Toast.LENGTH_LONG).show()
            } else {
                findNavController().navigate(SettingsFragmentRootDirections.actionSettingsFragmentRootToDriveLoginFragment(false, false))
            }

            true
        }

        findPreference<Preference>(PreferenceRepository.PREFERENCE_GDRIVE_EXPORT)?.setOnPreferenceClickListener {
            if (!manager.installedModules.contains(GDRIVE_MODULE_NAME)) {
                Toast.makeText(requireContext(), "Модуль еще не установлен", Toast.LENGTH_LONG).show()
            } else {
                findNavController().navigate(SettingsFragmentRootDirections.actionSettingsFragmentRootToDriveLoginFragment(true, false))
            }

            true
        }
    }

    private fun initLocalImExportListeners() {
        findPreference<Preference>(PreferenceRepository.PREFERENCE_LOCAL_EXPORT)?.setOnPreferenceClickListener {
            findNavController().navigate(SettingsFragmentRootDirections.actionSettingsFragmentRootToExportFragment(0, Export.DESTINATION_LOCAL))
            true
        }

        findPreference<Preference>(PreferenceRepository.PREFERENCE_LOCAL_IMPORT)?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/octet-stream"
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            backupPicker.launch(intent)

            true
        }
    }

    override fun onResume() {
        super.onResume()
        importViewModel.importState.observe(viewLifecycleOwner) {
            when(it) {
                is ProcessStates.Success -> {
                    val count = importViewModel.lastBackupRecordsCount()
                    if (count == 0) {
                        Toast.makeText(requireContext(), resources.getString(R.string.import_nodata), Toast.LENGTH_SHORT).show()
                        return@observe
                    }
                    Snackbar.make(requireView(), resources.getQuantityString(R.plurals.import_cancel, count, count), Snackbar.LENGTH_LONG)
                            .setAction(resources.getString(R.string.import_cancel)) {
                                importViewModel.rollbackLastImport()
                            }.show()
                }
                is ProcessStates.InProgress -> {

                }
                is ProcessStates.Failure -> {
                    Toast.makeText(requireContext(), resources.getString(R.string.import_error_readfile), Toast.LENGTH_LONG).show()
                }
            }
        }

        featureDownloadViewModel.installState.observe(viewLifecycleOwner) {
            when (it) {
                is FeatureDownloadViewModel.ProcessState.InProgress -> {
                    driveEnabled.isInProgress = true
                    when (it.state) {
                        is FeatureDownloadViewModel.InProgressState.Downloading -> {
                            Log.d(logTag, "Downloading")
                            driveEnabled.setProgress(it.state.max, it.state.progress, false)
                        }
                        is FeatureDownloadViewModel.InProgressState.Downloaded -> {
                            Log.d(logTag, "Downloaded")
                            driveEnabled.setProgress(0, 0, true)
                        }
                        is FeatureDownloadViewModel.InProgressState.Cancelling -> {
                            Log.d(logTag, "Cancelling")
                            driveEnabled.setProgress(0, 0, true)
                        }
                        is FeatureDownloadViewModel.InProgressState.Installing -> {
                            Log.d(logTag, "Installing")
                            driveEnabled.setProgress(0, 0, true)
                        }
                        is FeatureDownloadViewModel.InProgressState.Pending -> {
                            Log.d(logTag, "Pending")
                            driveEnabled.setProgress(0, 0, true)
                        }
                        is FeatureDownloadViewModel.InProgressState.RequiresUserConfirmation -> {
                            Log.d(logTag, "ReqUser Confirm")
                            driveEnabled.setProgress(0, 0, true)
                        }
                        is FeatureDownloadViewModel.InProgressState.Unknown -> {
                            Log.d(logTag, "Unknown")
                            driveEnabled.setProgress(0, 0, true)
                        }
                    }
                }
                is FeatureDownloadViewModel.ProcessState.Canceled -> {
                    Log.d(logTag, "Cancelled")
                    driveEnabled.isInProgress = false
                    driveEnabled.isChecked = false
                }
                is FeatureDownloadViewModel.ProcessState.Failure -> {
                    Log.d(logTag, "Failure")
                    driveEnabled.isInProgress = false
                    driveEnabled.isChecked = false
                }
                is FeatureDownloadViewModel.ProcessState.Success -> {
                    Log.d(logTag, "Success")
                    driveEnabled.isInProgress = false
                    driveEnabled.isChecked = true
                }
                null -> {
                }
            }
        }
    }
}