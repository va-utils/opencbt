package com.vva.androidopencbt

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity
import com.github.omadahealth.lollipin.lib.managers.LockManager
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class PinActivity: AppLockActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var isBackPressedOnce = false

    val TAG = "LOLLIPIN"
    override fun showForgotDialog() {
//        val intent = Intent(this, PinActivity::class.java)
//        intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN)
//        startActivity(intent)
        val phrase: String = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("pin_reset_phrase", "") ?: ""
        Log.d(TAG, phrase)
        if (phrase.isEmpty())
            Toast.makeText(this, "Фраза для восстановления не задана, сброс пин-кода невозможен", Toast.LENGTH_LONG).show()
        else {
            val view = LayoutInflater.from(this).inflate(R.layout.prompt, null)
            val editText = view.findViewById<EditText>(R.id.prompt_edittext)
            editText.transformationMethod = PasswordTransformationMethod.getInstance()

            AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton("OK") {
                        dialogInterface: DialogInterface, i: Int ->
                        if (phrase == editText.text.toString()) {
//                            startActivity(Intent(this, PinActivity::class.java).apply {
//                                putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN)
//                            })
                            LockManager.getInstance().disableAppLock()
                        } else {
                            Toast.makeText(this, "Фразы не совпадают", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("Отмена") {
                        dialogInterface: DialogInterface, i: Int ->
                    }
                    .show()
        }
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