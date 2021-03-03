package com.vva.androidopencbt.recordslist

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.vva.androidopencbt.App
import com.vva.androidopencbt.BuildConfig
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.export.ExportViewModel
import com.vva.androidopencbt.settings.ExportFormats
import com.vva.androidopencbt.settings.PreferenceRepository
import java.io.File

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var database: CbdDatabase
    private lateinit var prefs: PreferenceRepository
    private val listViewModel: RecordListViewModel by viewModels {
        RecordListViewModelFactory(database.databaseDao, prefs)
    }
    private val exportViewModel: ExportViewModel by activityViewModels()

    private lateinit var ll: LinearLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter
    private lateinit var welcomeTv: TextView
    private lateinit var fab : FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var appBar: AppBarLayout
    private var actionMode: ActionMode? = null
    private var itemForDeletionCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        appBar = view.findViewById(R.id.rv_appBar)
        toolbar = view.findViewById(R.id.rv_toolbar)
        toolbar.inflateMenu(R.menu.menu_main)

        toolbar.setupWithNavController(navController, appBarConfiguration)
        toolbar.menu.forEach { menuItem ->
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

        dataAdapter = RecordsAdapter(RecordListener { view: View, dbRecord: DbRecord, position: Int ->
            when (listViewModel.onItemClick(dbRecord)) {
                null -> {
                    findNavController().navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial().apply { recordKey = dbRecord.id })
                }
            }
        },
        RecordLongListener { view: View, dbRecord: DbRecord, position: Int ->
            listViewModel.onItemLongClick(dbRecord)
            viewModel.activateSelection()
            true
        })

        listViewModel.selectedItems.observe(viewLifecycleOwner) {
            dataAdapter.submitSelectionArray(it)
            itemForDeletionCount = it.filterValues { it }.size
            actionMode?.invalidate()
        }

        prefs.defaultExportFormat.observe(viewLifecycleOwner) {
            exportViewModel.format = it
        }

        exportViewModel.isExportFileReady.observe(viewLifecycleOwner) {
            val fileType = when (exportViewModel.format) {
                ExportFormats.JSON -> {
                    "application/octet-stream"
                }
                ExportFormats.HTML -> {
                    "application/html"
                }
                else -> {
                    throw IllegalArgumentException("No such format")
                }
            }
            if (it) {
                val file = File(requireActivity().filesDir, exportViewModel.fileName)
                val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file)
                val forSendIntent = Intent(Intent.ACTION_SEND)
                forSendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                forSendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                forSendIntent.setDataAndType(uri, fileType)

                val pm: PackageManager = requireActivity().packageManager
                if (forSendIntent.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(forSendIntent, getString(R.string.savehtml_text_share)))
                    exportViewModel.htmlFileShared()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.isSelectionActive.observe(viewLifecycleOwner) {
            if (!it) {
                listViewModel.cancelAllSelections()
            } else {
                actionMode = toolbar.startActionMode(object: ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        mode.menuInflater?.inflate(R.menu.list_selection, menu)
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                        menu.findItem(R.id.action_delete).isEnabled = itemForDeletionCount > 0
                        menu.findItem(R.id.action_export).isEnabled = itemForDeletionCount > 0
                        return true
                    }

                    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                        return when (item.itemId) {
                            R.id.action_delete -> {
                                val count = listViewModel.deleteSelected()
                                Snackbar.make(ll, resources.getQuantityString(R.plurals.delete_cancel, count, count), Snackbar.LENGTH_LONG).setAction(R.string.cancel) {
                                    listViewModel.rollbackDeletion()
                                }.show()
                                mode.finish()
                                true
                            }
                            R.id.action_select_all -> {
                                listViewModel.selectAll(dataAdapter.getList())
                                true
                            }
                            R.id.action_export -> {
                                exportViewModel.exportSelected(listViewModel.selectedItems.value?.keys?.toList(), requireContext())
                                mode.finish()
                                true
                            }
                            else -> {
                                mode.finish()
                                true
                            }
                        }
                    }

                    override fun onDestroyActionMode(mode: ActionMode?) {
                        viewModel.deactivateSelection()
                    }
                })
            }
        }

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

        dataAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                rv.layoutManager?.smoothScrollToPosition(rv, null, positionStart)
            }
        })

        prefs.isQuotesEnabled.observe(viewLifecycleOwner) {
            dataAdapter.quotes = it
        }

        prefs.isDividersEnabled.observe(viewLifecycleOwner) {
            dataAdapter.dividers = it
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
                            val count = list.size
                            Snackbar.make(ll, resources.getQuantityString(R.plurals.import_cancel, count, count), Snackbar.LENGTH_SHORT)
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