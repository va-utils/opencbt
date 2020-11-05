package com.vva.androidopencbt.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vva.androidopencbt.R
import com.vva.androidopencbt.getDateTimeString
import java.util.*

class StatisticFragment : Fragment() {

    private lateinit var countTextView : TextView
    private lateinit var avgIntensityTextView: TextView
    private lateinit var oldestTextView : TextView
    private lateinit var latestTextView : TextView
    private lateinit var ll : LinearLayout
    private val viewModel: StatisticViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.statistic_toolbar).setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        ll = inflater.inflate(R.layout.fragment_statistic, container, false) as LinearLayout

        countTextView = ll.findViewById(R.id.countTextView)
        oldestTextView = ll.findViewById(R.id.oldestTextView)
        latestTextView = ll.findViewById(R.id.latestTextView)
        avgIntensityTextView = ll.findViewById(R.id.avgintensityTextView)

        viewModel.getAllRecordsCount().observe(viewLifecycleOwner, {
            countTextView.text = getString(R.string.stat_total,it)
        })

        viewModel.getAverageIntensity().observe(viewLifecycleOwner, {
            avgIntensityTextView.text = getString(R.string.stat_intesity,it)
        })

        viewModel.getOldestRecordDate().observe(viewLifecycleOwner, {
            oldestTextView.text = getString(R.string.stat_old, Date(it).getDateTimeString())
        })

        viewModel.getLatestRecordDate().observe(viewLifecycleOwner, {
            latestTextView.text = getString(R.string.stat_latest, Date(it).getDateTimeString())
        })

        return ll
    }
}