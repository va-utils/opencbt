package com.vva.androidopencbt.recordslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var ll: ConstraintLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter
    private lateinit var welcomeTv: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as ConstraintLayout
        rv = ll.findViewById(R.id.rv)
        welcomeTv = ll.findViewById(R.id.welcomeTextView)

        dataAdapter = RecordsAdapter(RecordListener {
            it?.let {
                viewModel.navigateToRecord(it.id ?: 0)
            }
        }, ScrollListener {
//            viewModel.listUpdated()
//            if (it == 0) {
//                rv.adapter?.itemCount?.minus(1)?.let { it1 -> rv.smoothScrollToPosition(it1) }
//            } else {
//                rv.smoothScrollToPosition(0)
//            }
        })

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val orderBy = if (prefs.getBoolean("desc_ordering", true)) 0 else 1
        viewModel.getAllRecordsOrdered(orderBy).observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                dataAdapter.updateList(it, orderBy)
                welcomeTv.visibility = View.GONE
                rv.visibility = View.VISIBLE
                viewModel.listUpdated()
            } else {
                welcomeTv.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        })

        viewModel.recordsListUpdated.observe(viewLifecycleOwner, Observer {
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