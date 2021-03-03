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
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class DriveFileListFragment: Fragment() {
    private val job = Job()
    private val fragmentScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var mDriveServiceHelper: DriveServiceHelper
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var mDataAdapter: DriveListAdapter

    private val viewModel: DriveFileListViewModel by viewModels()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    private val requestDriveAccountActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        val resultIntent = it.data
        if (it.resultCode == Activity.RESULT_OK && resultIntent != null) {
            handleSignInResult(resultIntent)
        } else if (it.resultCode == Activity.RESULT_CANCELED) {
            findNavController().popBackStack()
        }
    }

    private lateinit var ll: LinearLayout
    private lateinit var toolbar: Toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        toolbar = view.findViewById<Toolbar>(R.id.rv_toolbar).apply {
            setupWithNavController(navController, appBarConfiguration)
            title = "Google Drive backups"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.fragment_list, container, false) as LinearLayout

        viewModel.isLogInSuccessful.observe(viewLifecycleOwner) {
            when(it) {
                null -> {
                    requestSignIn()
                }
                false -> {
                    findNavController().popBackStack()
                }
                true -> {
                    proceed()
                }
            }
        }

        return ll
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

                mDriveServiceHelper = DriveServiceHelper.getInstance(credential)
                viewModel.setLogInSuccessful()
            }
        } else {
            fragmentScope.launch(Dispatchers.IO) {
                try {
                    task.await()
                } catch (e: ApiException) {
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
                mDriveServiceHelper = DriveServiceHelper.getInstance(credential)
                withContext(Dispatchers.Main) {
                    viewModel.setLogInSuccessful()
                }
            } catch (e: ApiException) {
                withContext(Dispatchers.Main) {
                    viewModel.setLogInUnsuccessful()
                }
            }
        }
    }

    private fun proceed() {
        viewModel.driveServiceHelper = mDriveServiceHelper
        recyclerView = ll.findViewById(R.id.rv)
        swipeRefreshLayout = ll.findViewById(R.id.list_swipe)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshFileList()
        }

        mDataAdapter = DriveListAdapter()
        viewModel.driveFileList.observe(viewLifecycleOwner) {
            mDataAdapter.submitList(it)
        }

        recyclerView.adapter = mDataAdapter
        viewModel.isRequestIsActive.observe(viewLifecycleOwner) {
            swipeRefreshLayout.isRefreshing = it
        }
    }

    override fun onStop() {
        fragmentScope.cancel()
        super.onStop()
    }
}