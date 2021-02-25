package com.vva.androidopencbt.gdrivefeature

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*

class DriveFileListFragment: Fragment() {
    private lateinit var mDriveServiceHelper: DriveServiceHelper

    private lateinit var ll: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.fragment_list, container, false) as LinearLayout

        return ll
    }

    fun requestSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()

        val client = GoogleSignIn.getClient(requireActivity(), signInOptions)
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val resultIntent = it.data
            if (it.resultCode == Activity.RESULT_OK && resultIntent != null) {
                handleSignInResult(resultIntent)
            }
        }.launch(client.signInIntent)
    }

    private fun handleSignInResult(intent: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(intent)
                .addOnSuccessListener {
                    val credential = GoogleAccountCredential.usingOAuth2(requireContext(),
                            Collections.singleton(DriveScopes.DRIVE_FILE))
                    credential.selectedAccount = it.account
                    val drive = Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            GsonFactory(),
                            credential
                    ).setApplicationName("OpenCBT Drive Backup").build()

                    mDriveServiceHelper = DriveServiceHelper(drive)
                }
                .addOnFailureListener {
                    Log.e("EXCEPTION", "Unable sign in", it)
                }
    }

    private fun query() {
        mDriveServiceHelper.queryFiles()
                .addOnSuccessListener {

                }
    }

}