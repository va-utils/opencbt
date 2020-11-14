package com.vva.androidopencbt.recordslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
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
//                return@setOnMenuItemClickListener NavigationUI.onNavDestinationSelected(it, requireView().findNavController()) || super.onOptionsItemSelected(it)
                val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                findNavController().navigate(it.itemId, null, navOptions)
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
        }, ScrollListener {
            viewModel.listUpdated()
        })

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val orderBy = if (prefs.getBoolean("desc_ordering", true)) 0 else 1
        viewModel.getAllRecords().observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                dataAdapter.updateList(it, orderBy)
//                dataAdapter.submitList(it)
                welcomeTv.visibility = View.GONE
                rv.visibility = View.VISIBLE
            } else {
                welcomeTv.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        })

        viewModel.recordsListUpdated.observe(viewLifecycleOwner, {
            if (!it) {
                if (orderBy == 0) {
                    rv.smoothScrollToPosition(0)
                } else {
                    rv.adapter?.itemCount?.minus(1)?.let { it1 -> rv.smoothScrollToPosition(it1) }
                }
            }
        })
        rv.adapter = dataAdapter

        return ll
    }
}