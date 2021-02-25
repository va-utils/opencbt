package com.vva.androidopencbt

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.recordslist.RecordListViewModel
import com.vva.androidopencbt.recordslist.RecordListViewModelFactory
import com.vva.androidopencbt.recordslist.RvFragmentDirections
import com.vva.androidopencbt.settings.PreferenceRepository


@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {
    private val vm: RecordsViewModel by viewModels()
    private lateinit var preferences: PreferenceRepository
    private lateinit var database: CbdDatabase

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
                    startActivityForResult(i, 0x999)
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

    fun addNewRecord(view: View) {
        findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0x999) {
            if (resultCode == RESULT_CANCELED)
                this.finishAffinity()
            if (resultCode == RESULT_OK) {
                vm.authSuccessful()
            }
        }
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