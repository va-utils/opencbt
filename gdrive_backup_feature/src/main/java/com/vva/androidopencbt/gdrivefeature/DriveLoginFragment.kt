package com.vva.androidopencbt.gdrivefeature

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import com.vva.androidopencbt.MainActivity
import com.vva.androidopencbt.NavigationDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

@AndroidEntryPoint
class DriveLoginFragment: Fragment() {
    private val logTag = javaClass.canonicalName
    private val job = Job()
    private val fragmentScope = CoroutineScope(Dispatchers.Main + job)
    private val viewModel: DriveFileListViewModel by activityViewModels()
    lateinit var googleClient: GoogleSignInClient
    private lateinit var cl: ConstraintLayout

    private val requestDriveAccountActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        val resultIntent = it.data
        if (it.resultCode == Activity.RESULT_OK && resultIntent != null) {
            handleSignInResult(resultIntent)
        } else if (it.resultCode == Activity.RESULT_CANCELED) {
            val navOptions = NavOptions.Builder().setPopUpTo(com.vva.androidopencbt.R.id.rvFragment, false).build()
            findNavController().navigate(NavigationDirections.actionGlobalRvFragment(), navOptions)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        cl = inflater.inflate(R.layout.fragment_login, container, false) as ConstraintLayout

        requestSignIn()

        return cl
    }

    private fun requestSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()

        googleClient = GoogleSignIn.getClient(requireActivity(), signInOptions)
        val task = googleClient.silentSignIn()
        if (task.isSuccessful) {
            task.result?.let {
                val credential = GoogleAccountCredential.usingOAuth2(context,
                        Collections.singleton(DriveScopes.DRIVE_FILE)).apply {
                    selectedAccount = it.account
                }
                loginSuccessful(credential, it, googleClient)
            }
        } else {
            fragmentScope.launch(Dispatchers.IO) {
                try {
                    val account = task.await()
                    val credentials = GoogleAccountCredential.usingOAuth2(context,
                        listOf(DriveScopes.DRIVE_FILE)).apply {
                            selectedAccount = account.account
                    }
                    withContext(Dispatchers.Main) {
                        loginSuccessful(credentials, account, googleClient)
                    }
                } catch (e: ApiException) {
                    Log.e(logTag, "login exception", e)
                    when(e.statusCode) {
                        CommonStatusCodes.SIGN_IN_REQUIRED -> {
                            requestDriveAccountActivity.launch(googleClient.signInIntent)
                        }
                    }
                }
            }
        }
    }

    private fun handleSignInResult(intent: Intent) {
        fragmentScope.launch(Dispatchers.IO) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
                val credential = GoogleAccountCredential.usingOAuth2(requireContext(),
                        listOf(DriveScopes.DRIVE_FILE)).apply {
                    selectedAccount = account.account
                }
                withContext(Dispatchers.Main) {
                    loginSuccessful(credential, account, googleClient)
                }
            } catch (e: ApiException) {
                Log.e(logTag, "failed", e)
                loginFailed()
            }
        }
    }

    private fun loginSuccessful(credentials: GoogleAccountCredential, account: GoogleSignInAccount, client: GoogleSignInClient) {
        viewModel.driveCredentials = credentials
        viewModel.driveAccount = account
        viewModel.driveClient = client
        findNavController().popBackStack()
    }

    private fun loginFailed() {
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        job.cancel()

        super.onDestroy()
    }

    override fun onResume() {
        (requireActivity() as MainActivity).supportActionBar?.hide()
        super.onResume()
    }

    override fun onStop() {
        (requireActivity() as MainActivity).supportActionBar?.show()
        super.onStop()
    }
}