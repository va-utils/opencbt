package com.vva.androidopencbt.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.vva.androidopencbt.R
import com.vva.androidopencbt.getDateTimeString
import java.util.*

class StatisticFragment : Fragment() {

    private lateinit var countTextView : TextView
    private lateinit var avgintensityTextView: TextView
    private lateinit var oldestTextView : TextView;
    private lateinit var latestTextView : TextView;
    private lateinit var distortionsTextView : TextView;
    private lateinit var ll : LinearLayout
    private val viewModel: StatisticViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        ll = inflater.inflate(R.layout.fragment_statistic, container, false) as LinearLayout

        countTextView = ll.findViewById(R.id.countTextView)
        oldestTextView = ll.findViewById(R.id.oldestTextView);
        latestTextView = ll.findViewById(R.id.latestTextView);
        avgintensityTextView = ll.findViewById(R.id.avgintensityTextView);
        distortionsTextView = ll.findViewById(R.id.distortionTextView)

        viewModel.getAllRecordsCount().observe(viewLifecycleOwner, {
            if(it!=null)
                countTextView.text = getString(R.string.stat_total,it);
        })

        viewModel.getAverageIntensity().observe(viewLifecycleOwner, {
            avgintensityTextView.text = getString(R.string.stat_intesity,(it ?: 0.0))
        })

        viewModel.getOldestRecordDate().observe(viewLifecycleOwner, {
            val s : String = if(it==null)
                "---"
            else
                Date(it).getDateTimeString()

            oldestTextView.text = getString(R.string.stat_old,s /*Date(it).getDateTimeString()*/)
        })

        viewModel.getLatestRecordDate().observe(viewLifecycleOwner, {
            val s : String = if(it==null)
                "---"
            else
                Date(it).getDateTimeString()
            latestTextView.text = getString(R.string.stat_latest, s/*Date(it).getDateTimeString()*/)
        })

        viewModel.getDistortionsTop();

        viewModel.distortions.observe(viewLifecycleOwner,{
            val b : StringBuilder = StringBuilder()
            b.append(getString(R.string.dist_all_or_nothing)).append(": ").append(it[0]).appendLine()
            b.append(getString(R.string.dist_overgeneralizing)).append(": ").append(it[1]).appendLine()
            b.append(getString(R.string.dist_filtering)).append(": ").append(it[2]).appendLine()
            b.append(getString(R.string.dist_disqual_positive)).append(": ").append(it[3]).appendLine()
            b.append(getString(R.string.dist_jump_conclusion)).append(": ").append(it[4]).appendLine()
            b.append(getString(R.string.dist_magn_and_min)).append(": ").append(it[5]).appendLine()
            b.append(getString(R.string.dist_emotional_reasoning)).append(": ").append(it[6]).appendLine()
            b.append(getString(R.string.dist_must_statement)).append(": ").append(it[7]).appendLine()
            b.append(getString(R.string.dist_labeling)).append(": ").append(it[8]).appendLine()
            b.append(getString(R.string.dist_personalistion)).append(": ").append(it[9]).appendLine()
            distortionsTextView.text = b.toString()
        })
        return ll
    }
}