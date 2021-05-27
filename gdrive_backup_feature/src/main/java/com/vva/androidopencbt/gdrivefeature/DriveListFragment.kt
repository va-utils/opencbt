package com.vva.androidopencbt.gdrivefeature

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.vva.androidopencbt.export.ImportViewModel
import com.vva.androidopencbt.export.ProcessStates
import dagger.hilt.android.AndroidEntryPoint

const val ROOT_FOLDER = "OpenCBT"

@AndroidEntryPoint
class DriveListFragment: Fragment() {
    private val logTag = javaClass.canonicalName
    private lateinit var ll: LinearLayout
    private val driveViewModel: DriveFileListViewModel by activityViewModels()
    private val importViewModel: ImportViewModel by activityViewModels()
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.fragment_list, container, false) as LinearLayout
        rv = ll.findViewById(R.id.rv)
        sr = ll.findViewById(R.id.list_swipe)
        setHasOptionsMenu(true)

        when (driveViewModel.isLoggedIn()) {
            false -> {
                findNavController().navigate(DriveListFragmentDirections.actionDriveListFragmentToDriveLoginFragment())
            }
            true -> {
                recyclerViewInit()
                importStateSubscribe()
                requestSubscriptions()

                val args = DriveListFragmentArgs.fromBundle(requireArguments())
                if (args.fileName.isNotEmpty() && args.filePath.isNotEmpty()) {
                    driveViewModel.uploadFileAppRoot(args.fileName, args.filePath)
                } else {
                    driveViewModel.getFileList()
                }
            }
        }
        return ll
    }

    private fun recyclerViewInit() {
        val adapter = DriveListAdapter {
            importViewModel.importFromList {
                driveViewModel.readFile(it)
            }
        }

        driveViewModel.driveFileList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        rv.adapter = adapter
    }

    private fun importStateSubscribe() {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                driveViewModel.signOut()
                findNavController().popBackStack()
                true
            }
            else -> {
                true
            }
        }
    }
}