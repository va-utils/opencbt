package com.vva.androidopencbt.recordslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var ll: LinearLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as LinearLayout
        rv = ll.findViewById(R.id.rv)
        dataAdapter = RecordsAdapter()
        
        viewModel.getAllRecords().observe(viewLifecycleOwner, Observer {
            dataAdapter.submitList(it)
        })
        rv.adapter = dataAdapter
        
        return ll
    }
}