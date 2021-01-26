package com.vva.androidopencbt

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
}