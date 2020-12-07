package com.vva.androidopencbt

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.recordslist.RvFragmentDirections


@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {
    private lateinit var vm: RecordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        vm = ViewModelProvider(this).get(RecordsViewModel::class.java)
        if (preferences.getBoolean("enable_pin_protection", false)) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i = km.createConfirmDeviceCredentialIntent(null, null)
                startActivityForResult(i, 0x999)
            }
        }

        vm.newRecordNavigated.observe(this, { aLong: Long ->
//            findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragment().apply { recordKey = aLong })
            findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial().apply { recordKey = aLong })
        })
    }

    fun addNewRecord(view: View) {
//        findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragment())
        findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x999) {
            if (resultCode == RESULT_CANCELED)
                this.finishAffinity()
            if (resultCode == RESULT_OK)
                vm.authSuccessful()
        }
    }
}