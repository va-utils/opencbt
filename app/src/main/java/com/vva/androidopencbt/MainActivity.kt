package com.vva.androidopencbt

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.amirarcane.lockscreen.activity.EnterPinActivity
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.vva.androidopencbt.recordslist.RvFragmentDirections


class MainActivity : AppCompatActivity() {
    private lateinit var vm: RecordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (preferences.getBoolean("enable_pin_protection", false)) {
//            val intent: Intent = EnterPinActivity.getIntent(applicationContext, false)
//            startActivity(intent)
            val intent = Intent(this, PinActivity::class.java)
            startActivity(intent)
        }
//        val intent = Intent(this, PinActivity::class.java)
//        intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
//        startActivityForResult(intent, 0x11)

        vm = ViewModelProvider(this).get(RecordsViewModel::class.java)

        vm.newRecordNavigated.observe(this, { aLong: Long ->
            findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragment().apply { recordKey = aLong })
        })
    }

    fun addNewRecord(view: View) {
        findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragment())
    }
}