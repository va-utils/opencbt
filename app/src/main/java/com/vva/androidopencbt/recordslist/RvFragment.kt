package com.vva.androidopencbt.recordslist

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.vva.androidopencbt.*
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.export.Export
import com.vva.androidopencbt.export.ExportFragmentDirections
import com.vva.androidopencbt.export.ExportStates
import com.vva.androidopencbt.export.ExportViewModel
import com.vva.androidopencbt.settings.ExportFormats
import com.vva.androidopencbt.settings.PreferenceRepository
import java.io.File

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var database: CbdDatabase
    private lateinit var prefs: PreferenceRepository
    private val listViewModel: RecordListViewModel by activityViewModels {
        RecordListViewModelFactory(database.databaseDao, prefs)
    }
    private val exportViewModel: ExportViewModel by activityViewModels()

    private lateinit var ll: LinearLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter
    private lateinit var welcomeTv: TextView
    private lateinit var fab : FloatingActionButton
    private lateinit var toolbar: Toolbar
//    private lateinit var appBar: AppBarLayout
    private var actionMode: ActionMode? = null
    private var itemForDeletionCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

//        val navController = findNavController()
//        val appBarConfiguration = AppBarConfiguration(navController.graph)
//        appBar = view.findViewById(R.id.rv_appBar)
//        toolbar = view.findViewById(R.id.rv_toolbar)
//        toolbar.inflateMenu(R.menu.menu_main)

//        toolbar.setupWithNavController(navController, appBarConfiguration)
//        val navOptions = NavOptions.Builder()
//                .setEnterAnim(R.anim.slide_in_right)
//                .setExitAnim(R.anim.slide_out_left)
//                .setPopEnterAnim(R.anim.slide_in_left)
//                .setPopExitAnim(R.anim.slide_out_right)
//                .build()
//        toolbar.menu.forEach { menuItem ->
//            if (menuItem.hasSubMenu())
//                menuItem.subMenu.forEach {
//                    it.setOnMenuItemClickListener {
//                        findNavController().navigate(it.itemId, null, navOptions)
//
//                        super.onOptionsItemSelected(it)
//                    }
//                }
//            menuItem.setOnMenuItemClickListener {
//                when (it.itemId) {
//                    R.id.share -> {
//                        findNavController().navigate(RvFragmentDirections.actionRvFragmentToExportFragment(Export.FORMAT_PICK, Export.DESTINATION_LOCAL))
//                    }
//                    R.id.hidden_group -> {
//                        super.onOptionsItemSelected(it)
//                    }
//                    else -> {
//                        findNavController().navigate(it.itemId, null, navOptions)
//                    }
//                }
//
//                return@setOnMenuItemClickListener super.onOptionsItemSelected(it)
//            }
//        }
        Log.d("TEEEST", "onViewCreated")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.record_motion_duration).toLong()
        }

        prefs = (requireActivity().application as App).preferenceRepository
        database = CbdDatabase.getInstance(requireContext())
        setHasOptionsMenu(true)
        Log.d("TEEEST", "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as LinearLayout

        rv = ll.findViewById(R.id.rv)
        welcomeTv = ll.findViewById(R.id.welcomeTextView)
        fab = ll.findViewById(R.id.fab)

        dataAdapter = RecordsAdapter(RecordListener { view: View, dbRecord: DbRecord, _: Int ->
            when (listViewModel.onItemClick(dbRecord)) {
                null -> {
                    exitTransition = MaterialElevationScale(false).apply {
                        duration = resources.getInteger(R.integer.record_motion_duration).toLong()
                    }
                    reenterTransition = MaterialElevationScale(true).apply {
                        duration = resources.getInteger(R.integer.record_motion_duration).toLong()
                    }
                    val transName = getString(R.string.record_card_view_detail_transition_name)
                    val extras = FragmentNavigatorExtras(view to transName)
                    val directions = RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial().apply {
                        recordKey = dbRecord.id
                    }
                    findNavController().navigate(directions, extras)
//                    findNavController().navigate(RvFragmentDirections.actionRvFragmentToDetailsFragmentMaterial().apply { recordKey = dbRecord.id })
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

        viewModel.isSelectionActive.observe(viewLifecycleOwner) {
            if (!it) {
                listViewModel.cancelAllSelections()
            } else {
                actionMode = (requireActivity() as MainActivity).startActionMode(object: ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                        mode.menuInflater?.inflate(R.menu.list_selection, menu)
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                        menu.findItem(R.id.action_delete).isEnabled = itemForDeletionCount > 0
                        menu.findItem(R.id.action_export).isEnabled = itemForDeletionCount > 0
                        menu.findItem(R.id.action_export_cloud).apply {
                            isEnabled = itemForDeletionCount > 0
                            isVisible = prefs.isDriveIntegrationEnabled.value == true
                        }
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
                                val export = Export.Builder()
                                        .setFormat(prefs.defaultExportFormat.value ?: ExportFormats.JSON)
                                        .setFileName("CBT_diary_selected")
                                        .setExportList(listViewModel.selectedItems.value?.keys?.toList()!!)
                                        .build()
                                exportViewModel.export(export)
                                mode.finish()
                                true
                            }
                            R.id.action_export_cloud -> {
                                val export = Export.Builder()
                                        .setFormat(prefs.defaultExportFormat.value ?: ExportFormats.JSON)
                                        .setFileName("CBT_diary_selected")
                                        .setExportList(listViewModel.selectedItems.value?.keys?.toList()!!)
                                        .cloud()
                                        .build()

                                exportViewModel.export(export)
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

        prefs.isQuotesEnabled.observe(viewLifecycleOwner) {
            dataAdapter.quotes = it
        }

        prefs.isDividersEnabled.observe(viewLifecycleOwner) {
            dataAdapter.dividers = it
        }

        val dialog = makeIndeterminateProgressDialog()
        exportViewModel.exportState.observe(viewLifecycleOwner) {
            when (it) {
                is ExportStates.Failure -> {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Something goes wrong", Toast.LENGTH_LONG).show()
                }
                is ExportStates.InProgress -> {
                    dialog.show()

                }
                is ExportStates.Success -> {
                    dialog.dismiss()
                    when (it.isCloud) {
                        false -> {
                            (requireActivity() as MainActivity).sendLocalFile(it.filePath)
                        }
                        true -> {
                            findNavController()
                                    .navigate(NavigationDirections.actionGlobalDriveListFragment(it.fileName, it.filePath))
                        }
                    }

                }
                null -> {
                    dialog.dismiss()
                }
            }
        }

        Log.d("TEEEST", "onCreateView")
        rv.adapter = dataAdapter

        return ll
    }

    private fun makeIndeterminateProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setBackground(ResourcesCompat.getDrawable(resources, R.drawable.pg_back, null))
                .setView(R.layout.progress_bar)
                .setCancelable(false)
                .create()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController()
        return when (item.itemId) {
            R.id.share -> {
                navController.navigate(RvFragmentDirections.actionRvFragmentToExportFragment(Export.FORMAT_PICK, Export.DESTINATION_LOCAL))
                true
            }
            R.id.hidden_group -> {
                true
            }
            R.id.settingsFragmentRoot -> {
                navController.navigate(item.itemId)
                true
            }
            R.id.statisticFragment -> {
                navController.navigate(item.itemId)
                true
            }
            R.id.detailsFragmentMaterial -> {
                navController.navigate(item.itemId)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}