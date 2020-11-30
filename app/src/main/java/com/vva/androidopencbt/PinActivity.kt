package com.vva.androidopencbt

import android.util.Log
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity

class PinActivity: AppLockActivity() {
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



}