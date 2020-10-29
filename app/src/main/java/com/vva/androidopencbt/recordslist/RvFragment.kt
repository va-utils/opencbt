package com.vva.androidopencbt.recordslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var ll: ConstraintLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as ConstraintLayout
        rv = ll.findViewById(R.id.rv)
        dataAdapter = RecordsAdapter(RecordListener {
            it?.let {
                viewModel.navigateToRecord(it.id ?: 0)
            }
        })
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val orderby = if (prefs.getBoolean("desc_ordering", true)) 0 else 1
        viewModel./*getAllRecords()*/getAllRecordsOrdered(orderby).observe(viewLifecycleOwner, Observer {
            dataAdapter.submitList(it)
        })
        rv.adapter = dataAdapter
        
        return ll
    }
}