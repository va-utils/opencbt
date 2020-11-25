package com.vva.androidopencbt.recordslist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var ll: LinearLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter
    private lateinit var welcomeTv: TextView

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
                            type = "application/json"
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as LinearLayout
        rv = ll.findViewById(R.id.rv)
        welcomeTv = ll.findViewById(R.id.welcomeTextView)

        dataAdapter = RecordsAdapter(RecordListener {
            viewModel.navigateToRecord(it.id)
        })

        viewModel.getAllRecords().observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                dataAdapter.submitList(it)

                welcomeTv.visibility = View.GONE
                rv.visibility = View.VISIBLE
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

        rv.adapter = dataAdapter

        viewModel.importInAction.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe
            else if (!it) {
                if (viewModel.importData.value == null) {
                    Toast.makeText(requireContext(), "Ошибки при чтении файла", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.importData.value.let { list ->
                        if (list?.isEmpty()!!) {
                            Toast.makeText(requireContext(), "Нет данных для импорта", Toast.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(ll, "Импортировано ${list.size} записей", Snackbar.LENGTH_SHORT)
                                    .setAction("Отмена") { _ ->
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