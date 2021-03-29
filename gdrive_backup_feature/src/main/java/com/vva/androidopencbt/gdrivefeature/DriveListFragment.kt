package com.vva.androidopencbt.gdrivefeature

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.export.ImportViewModel
import com.vva.androidopencbt.export.ImportViewModelFactory
import com.vva.androidopencbt.export.ProcessStates
import kotlin.math.log

const val ROOT_FOLDER = "OpenCBT"

class DriveListFragment: Fragment() {
    private val logTag = javaClass.canonicalName
    private lateinit var ll: LinearLayout
    private val driveViewModel: DriveFileListViewModel by activityViewModels()
    private lateinit var dao: RecordDao
    private val importViewModel: ImportViewModel by activityViewModels {
        ImportViewModelFactory(dao)
    }
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar = view.findViewById<Toolbar>(R.id.rv_toolbar)

        toolbar.setupWithNavController(navController, appBarConfiguration)
        toolbar.inflateMenu(R.menu.list_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.log_out -> {
                    driveViewModel.signOut()
                }
            }
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.fragment_list, container, false) as LinearLayout
        rv = ll.findViewById(R.id.rv)
        sr = ll.findViewById(R.id.list_swipe)

        dao = CbdDatabase.getInstance(requireContext()).databaseDao
        if (driveViewModel.driveClient == null || driveViewModel.driveAccount?.isExpired == true)
            findNavController().navigate(DriveListFragmentDirections.actionDriveListFragmentToDriveLoginFragment(false, true))

        driveViewModel.isLoginSuccessful.observe(viewLifecycleOwner) {
            when (it) {
                false -> {
                    Log.d(logTag, "logOut")
                    findNavController().popBackStack()
                }
            }
        }

        val adapter = DriveListAdapter(
                OnClickListener {
                    importViewModel.importFromList {
                        driveViewModel.readFile(it)
                    }
                }
        )

        val dialog = blockingProgressDialog()
        importViewModel.importState.observe(viewLifecycleOwner) {
            when (it) {
                is ProcessStates.InProgress -> {
                    Log.d(logTag, "importInProgress")
                    dialog.show()
                }
                is ProcessStates.Success -> {
                    dialog.dismiss()
                    Log.d(logTag, "importSucceeded")
                    val count = importViewModel.lastBackupRecordsCount()
                    Log.d(logTag, importViewModel.lastBackupRecordsCount().toString())
                    val snack = Snackbar.make(requireView(), resources.getQuantityString(com.vva.androidopencbt.R.plurals.import_cancel, count, count), Snackbar.LENGTH_LONG)
                    if (count > 0) {
                        snack.setAction(resources.getString(com.vva.androidopencbt.R.string.import_cancel)) {
                            importViewModel.rollbackLastImport()
                        }
                    }
                    snack.show()
                }
                is ProcessStates.Failure -> {
                    Log.d(logTag, "importFail")
                    dialog.dismiss()
                }
                null -> {
                    Log.d(logTag, "importEnded")
                    dialog.dismiss()
                }
            }
        }

        val args = DriveListFragmentArgs.fromBundle(requireArguments())
        if (args.fileName.isNotEmpty() && args.filePath.isNotEmpty()) {
            driveViewModel.uploadFileAppRoot(args.fileName, args.filePath)
        } else {
            driveViewModel.getFileList()
        }

        driveViewModel.driveFileList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        requestSubscriptions()

        rv.adapter = adapter

        return ll
    }

    private fun requestSubscriptions() {
        sr.setOnRefreshListener {
            driveViewModel.getFileList()
        }

        driveViewModel.requestStatus.observe(viewLifecycleOwner) {
            when (it) {
                is RequestStatus.Success -> {
                    sr.isRefreshing = false
                }
                is RequestStatus.InProgress -> {
                    sr.isRefreshing = true
                }
                is RequestStatus.Failure -> {
                    sr.isRefreshing = false
                }
                null -> {
                    sr.isRefreshing = false
                }
            }
        }

        val dialog = blockingProgressDialog()
        driveViewModel.blockingRequestStatus.observe(viewLifecycleOwner) {
            when (it) {
                is RequestStatus.Success -> {
                    dialog.dismiss()
                }
                is RequestStatus.InProgress -> {
                    dialog.show()
                }
                is RequestStatus.Failure -> {
                    dialog.dismiss()
                }
                null -> {
                    dialog.dismiss()
                }
            }
        }
    }

    private fun blockingProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setBackground(ResourcesCompat.getDrawable(resources, R.drawable.pg_back, null))
                .setView(R.layout.progress_bar)
                .setCancelable(false)
                .create()
    }
}