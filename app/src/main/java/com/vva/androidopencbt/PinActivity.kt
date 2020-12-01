package com.vva.androidopencbt

import android.app.Application
import android.util.Log
import android.util.TimeUtils
import android.widget.Toast
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class PinActivity: AppLockActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var isBackPressedOnce = false

    val TAG = "LOLLIPIN"
    override fun showForgotDialog() {
        Log.d(TAG, "pinForgot")
    }

    override fun onPinFailure(attempts: Int) {
        Log.d(TAG, "pinFail")
    }

    override fun onPinSuccess(attempts: Int) {
        Log.d(TAG, "pinSuccess")
    }

    override fun onBackPressed() {
        if (this.intent.hasExtra(AppLock.EXTRA_TYPE))
            super.onBackPressed()
        else if (isBackPressedOnce) {
            this.finishAffinity()
        } else {
            Toast.makeText(this, getString(R.string.pin_back), Toast.LENGTH_SHORT).show()

            isBackPressedOnce = true
            uiScope.launch {
                withContext(Dispatchers.Default) {
                    delay(TimeUnit.SECONDS.toMillis(2))
                }
                isBackPressedOnce = false
            }
        }
    }

}