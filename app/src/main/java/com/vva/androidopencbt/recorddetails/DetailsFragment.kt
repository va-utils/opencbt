package com.vva.androidopencbt.recorddetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.vva.androidopencbt.R

class DetailsFragment: Fragment() {
    private lateinit var ll: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.activity_new_record, container, false) as LinearLayout

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}