package com.vva.androidopencbt

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.play.core.splitcompat.SplitCompat
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.recordslist.RecordListViewModel
import com.vva.androidopencbt.recordslist.RecordListViewModelFactory
import com.vva.androidopencbt.recordslist.RvFragmentDirections
import com.vva.androidopencbt.settings.PreferenceRepository
import java.io.File


@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {
    private val vm: RecordsViewModel by viewModels()
    private lateinit var preferences: PreferenceRepository
    private lateinit var database: CbdDatabase
    private val authActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_CANCELED)
            this.finishAffinity()
        if (it.resultCode == RESULT_OK) {
            vm.authSuccessful()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotifyChannels()

        preferences = (application as App).preferenceRepository
        database = CbdDatabase.getInstance(this)

        vm.isAuthenticated.observe(this) {
            if (!it && preferences.isPinEnabled.value == true) {
                val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (km.isKeyguardSecure) {
                    val i = km.createConfirmDeviceCredentialIntent(null, null)
                    authActivity.launch(i)
                }
            } else {
                preferences.isNightThemeLive.observe(this) {
                    if (it) {
                        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
                    } else {
                        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
                    }
                }
            }
        }

        vm.askDetailsFragmentConfirm.observe(this) {
            when (it) {
                false -> {
                    super.onBackPressed()
                }
            }
        }
    }

    fun sendLocalFile(filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file)
        val forSendIntent = Intent(Intent.ACTION_SEND)
        forSendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        forSendIntent.putExtra(Intent.EXTRA_STREAM, uri)
        forSendIntent.setDataAndType(uri, "application/octet-stream")

        val pm: PackageManager = this.packageManager
        if (forSendIntent.resolveActivity(pm) != null) {
            startActivity(Intent.createChooser(forSendIntent, getString(R.string.savehtml_text_share)))
        } else {
            Toast.makeText(this, getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun addNewRecord(view: View) {
        findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial())
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.myNavHostFragment)

        if (navController.currentDestination?.id == R.id.detailsFragmentMaterial) {
            vm.askDetailsFragmentConfirmation()
        } else  if (navController.currentDestination?.id == R.id.rvFragment && vm.isSelectionActive.value == true) {
            vm.deactivateSelection()
        } else {
            super.onBackPressed()
        }
    }

    private fun createNotifyChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.downloads_channel_name)
            val descriptionText = getString(R.string.downloads_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(DOWNLOADS_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val nm = ContextCompat.getSystemService(applicationContext,
                    NotificationManager::class.java) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}