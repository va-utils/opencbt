package com.vva.androidopencbt.statistic

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.vva.androidopencbt.R
import com.vva.androidopencbt.getDateTimeString
import kotlinx.android.synthetic.main.fragment_statistic.*
import kotlinx.android.synthetic.main.fragment_statistic.view.*

class StatisticFragment : Fragment() {

    //--общая
    private lateinit var oldTv : TextView
    private lateinit var latestTv : TextView
    private lateinit var totalTv : TextView
    private lateinit var intensityTv : TextView
    //--Времена суток
    private lateinit var nightTv : TextView
    private lateinit var morningTv : TextView
    private lateinit var dayTv : TextView
    private lateinit var eveningTv : TextView
    //искажения
    private lateinit var allOrNothingTv : TextView
    private lateinit var overgeneralizingTv : TextView
    private lateinit var filteringTv : TextView
    private lateinit var disqualTv : TextView
    private lateinit var jumpTv : TextView
    private lateinit var magnAndMinTv : TextView
    private lateinit var emotionalTv : TextView
    private lateinit var mustTv : TextView
    private lateinit var labelingTv : TextView
    private lateinit var personTv : TextView
    private lateinit var emptyTv : TextView
    //графики
    private lateinit var timeDonut : DonutProgressView
    private lateinit var distortionsDonut : DonutProgressView
    //-----
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

        totalTv = ll.findViewById(R.id.totalTv)
        oldTv = ll.findViewById(R.id.oldTv)
        latestTv = ll.findViewById(R.id.latestTv)

        timeDonut = ll.findViewById(R.id.timeDonut)
        nightTv = ll.findViewById(R.id.nightTv)
        morningTv = ll.findViewById(R.id.morningTv)
        dayTv = ll.findViewById(R.id.dayTv)
        eveningTv = ll.findViewById(R.id.eveningTv)

        distortionsDonut = ll.findViewById(R.id.distortionDonut)
        allOrNothingTv = ll.findViewById(R.id.allOrNothingTv)
        overgeneralizingTv = ll.findViewById(R.id.overgeneralizingTv)
        filteringTv = ll.findViewById(R.id.filteringTv)
        disqualTv = ll.findViewById(R.id.disqualTv)
        jumpTv = ll.findViewById(R.id.jumpTv)
        magnAndMinTv = ll.findViewById(R.id.magnMinTv)
        emotionalTv = ll.findViewById(R.id.emoReasonTv)
        mustTv = ll.findViewById(R.id.mustTv)
        labelingTv = ll.findViewById(R.id.labelingTv)
        personTv = ll.findViewById(R.id.personTv)
        emptyTv = ll.findViewById(R.id.emptyTv)

        intensityTv = ll.findViewById(R.id.intensityTv)


        

        viewModel.getAllRecordsCount().observe(viewLifecycleOwner, {
            if (it != null) {
                totalTv.text = it.toString()
              //  Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.getAverageIntensity().observe(viewLifecycleOwner, {
                intensityTv.text  = it.toString() ?: "0.0"
        })

        viewModel.getOldestRecordDate().observe(viewLifecycleOwner, {
            val s : String = it?.getDateTimeString() ?: "---"
            oldTv.text = s
        })

        viewModel.getLatestRecordDate().observe(viewLifecycleOwner, {
            val s : String = it?.getDateTimeString() ?: "---"
            latestTv.text = s
        })

        viewModel.getDistortionsTop()

        viewModel.distortions.observe(viewLifecycleOwner,{
            //TODO : перенесу все это в ресурсы, незачем строить строку
            allOrNothingTv.text = it[0].toString()
            overgeneralizingTv.text = it[1].toString()
            filteringTv.text = it[2].toString()
            disqualTv.text = it[3].toString()
            jumpTv.text = it[4].toString()
            magnAndMinTv.text = it[5].toString()
            emotionalTv.text = it[6].toString()
            mustTv.text = it[7].toString()
            labelingTv.text = it[8].toString()
            personTv.text = it[9].toString()
            emptyTv.text = it[10].toString()

            //отобразить на donut
            val list : List<DonutSection> = listOf(
                    DonutSection(getString(R.string.dist_all_or_nothing), resources.getColor(R.color.colorAllOrNothing),it[0].toFloat()),
                    DonutSection(getString(R.string.dist_overgeneralizing), resources.getColor(R.color.colorOvergeneralizing),it[1].toFloat()),
                    DonutSection(getString(R.string.dist_filtering), resources.getColor(R.color.colorFiltering),it[2].toFloat()),
                    DonutSection(getString(R.string.dist_disqual_positive), resources.getColor(R.color.colorDisqual),it[3].toFloat()),
                    DonutSection(getString(R.string.dist_jump_conclusion), resources.getColor(R.color.colorJump),it[4].toFloat()),
                    DonutSection(getString(R.string.dist_magn_and_min), resources.getColor(R.color.colorMagnMin),it[5].toFloat()),
                    DonutSection(getString(R.string.dist_emotional_reasoning), resources.getColor(R.color.colorEmotional),it[6].toFloat()),
                    DonutSection(getString(R.string.dist_must_statement), resources.getColor(R.color.colorMust),it[7].toFloat()),
                    DonutSection(getString(R.string.dist_labeling), resources.getColor(R.color.colorLabeling),it[8].toFloat()),
                    DonutSection(getString(R.string.dist_personalistion), resources.getColor(R.color.colorPerson),it[9].toFloat()))
                    DonutSection("Не заполнено", resources.getColor(R.color.colorEmpty),it[10].toFloat())

       //     distortionDonut.cap = it.sum().toFloat()
            distortionDonut.submitData(list)

        })

        viewModel.getTimeOfDay()
        viewModel.timesOfDay.observe(viewLifecycleOwner,
                {
                    nightTv.text = it[0].toString()
                    morningTv.text = it[1].toString()
                    dayTv.text = it[2].toString()
                    eveningTv.text = it[3].toString()

                    var list = listOf(
                            DonutSection("Night",resources.getColor(R.color.colorNight),it[0].toFloat()),
                            DonutSection("Morning",resources.getColor(R.color.colorMorning),it[1].toFloat()),
                            DonutSection("Day",resources.getColor(R.color.colorDay),it[2].toFloat()),
                            DonutSection("Evening",resources.getColor(R.color.colorEvening),it[3].toFloat()),
                    )
                    timeDonut.submitData(list)
                })

        return ll
    }
}