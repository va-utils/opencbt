package com.vva.androidopencbt.gdrivefeature

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vva.androidopencbt.RecordsViewModel

class DriveListFragment: Fragment() {
    private lateinit var ll: LinearLayout
    private val mainViewModel: RecordsViewModel by activityViewModels()
    private val viewModel: DriveFileListViewModel by activityViewModels()
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
                    viewModel.signOut()
                }
            }
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.fragment_list, container, false) as LinearLayout
        rv = ll.findViewById(R.id.rv)
        sr = ll.findViewById(R.id.list_swipe)

        sr.setOnRefreshListener {
            viewModel.getFileList()
        }

        val adapter = DriveListAdapter(
                OnClickListener {

                }
        )

        if (mainViewModel.exportJsonString.isNotEmpty()) {
            viewModel.saveFile(null, mainViewModel.exportJsonString, "plain/text")
            viewModel.getFileList()
        }

        viewModel.getFileList()
        viewModel.driveFileList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.requestStatus.observe(viewLifecycleOwner) {
            when (it) {
                is RequestStatus.Success -> {
                    sr.isRefreshing = false
                }
                is RequestStatus.InProgress -> {
                    sr.isRefreshing = true
                }
                is RequestStatus.Failure -> {
                    Log.e("DRIVE_FEATURE", "request error", it.e)
                    sr.isRefreshing = false
                }
                null -> {
                    sr.isRefreshing = false
                }
            }
        }

        rv.adapter = adapter

        return ll
    }
}