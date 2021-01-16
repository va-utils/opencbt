package com.vva.androidopencbt.recordslist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.vva.androidopencbt.App
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.settings.PreferenceRepository

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var database: CbdDatabase
    private lateinit var prefs: PreferenceRepository
    private val listViewModel: RecordListViewModel by viewModels {
        RecordListViewModelFactory(database.databaseDao, prefs)
    }

    private lateinit var ll: LinearLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter
    private lateinit var welcomeTv: TextView
    private lateinit var fab : FloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.rv_toolbar).setupWithNavController(navController, appBarConfiguration)
        view.findViewById<Toolbar>(R.id.rv_toolbar).menu.forEach { menuItem ->
            menuItem.setOnMenuItemClickListener {
                val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                when (val id = it.itemId) {
                    R.id.openDocumentPicker -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            type = "application/octet-stream"
                            addCategory(Intent.CATEGORY_DEFAULT)
                        }
                        startActivityForResult(intent, 0x33)
                    }
                    else -> {
                        findNavController().navigate(id, null, navOptions)
                    }
                }
                return@setOnMenuItemClickListener super.onOptionsItemSelected(it)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as LinearLayout

        rv = ll.findViewById(R.id.rv)
        welcomeTv = ll.findViewById(R.id.welcomeTextView)
        fab = ll.findViewById(R.id.fab)

        prefs = (requireActivity().application as App).preferenceRepository
        database = CbdDatabase.getInstance(requireContext())

        dataAdapter = RecordsAdapter(RecordListener {
            findNavController().navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial().apply { recordKey = it.id })
        },
        RecordLongListener { view: View, dbRecord: DbRecord ->
            listViewModel.activateSelection()
            view.callOnClick()
        })

        prefs.isIntensityIndicationEnabled.observe(viewLifecycleOwner) {
            dataAdapter.intensityIndication = it
        }

        viewModel.isAuthenticated.observe(viewLifecycleOwner, {
            if (it) {
                listViewModel.getAllRecords().observe(viewLifecycleOwner) { list ->
                    if (list.isNotEmpty()) {
                        dataAdapter.submitList(list)

                        welcomeTv.visibility = View.GONE
                        rv.visibility = View.VISIBLE
                    } else {
                        welcomeTv.visibility = View.VISIBLE
                        rv.visibility = View.GONE
                    }
                }
            } else {
                welcomeTv.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        })

//        viewModel.getAllRecords().observe(viewLifecycleOwner, {
//            if (it.isNotEmpty()) {
//                dataAdapter.submitList(it)
//
//                welcomeTv.visibility = View.GONE
//                rv.visibility = View.VISIBLE
//            } else {
//                welcomeTv.visibility = View.VISIBLE
//                rv.visibility = View.GONE
//            }
//        })

        dataAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                rv.layoutManager?.smoothScrollToPosition(rv, null, positionStart)
            }
        })

        viewModel.isQuotesEnabled.observe(viewLifecycleOwner) {
            dataAdapter.quotes = it
        }

        rv.adapter = dataAdapter

        viewModel.importInAction.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe
            else if (!it) {
                if (viewModel.importData.value == null) {
                    Toast.makeText(requireContext(), getString(R.string.import_error_readfile), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.importData.value.let { list ->
                        if (list?.isEmpty()!!) {
                            Toast.makeText(requireContext(), getString(R.string.import_nodata), Toast.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(ll, "Импортировано ${list.size} записей", Snackbar.LENGTH_SHORT)
                                    .setAction(getString(R.string.import_cancel)) { _ ->
                                        list.forEach { id ->
                                            viewModel.deleteRecord(id)
                                        }
                                    }.show()
                        }
                    }
                }
                viewModel.doneImporting()
            }
        }

        return ll
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0x33 && resultCode == Activity.RESULT_OK) {
            data?.data?.also {
                requireActivity().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.importRecordsFromFile(it, requireContext())
            }
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.recyclerViewState = rv.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()

        viewModel.restoreRecyclerView(rv)
    }
}