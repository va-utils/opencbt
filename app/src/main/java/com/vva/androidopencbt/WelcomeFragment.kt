package com.vva.androidopencbt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class WelcomeFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.getAllRecords().observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToRvFragment2())
            }
        })
        return inflater.inflate(R.layout.content_main, container, false)
    }
}