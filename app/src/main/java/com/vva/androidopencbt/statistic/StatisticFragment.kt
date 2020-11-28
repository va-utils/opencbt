package com.vva.androidopencbt.statistic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.vva.androidopencbt.R
import com.vva.androidopencbt.getStatsDateTime

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
  //  private lateinit var emptyTv : TextView
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

        distortionsDonut = ll.findViewById(R.id.distortionsDonut)
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
     //   emptyTv = ll.findViewById(R.id.emptyTv)

        intensityTv = ll.findViewById(R.id.intensityTv)


        

        viewModel.getAllRecordsCount().observe(viewLifecycleOwner, {
            if (it != null) {
                totalTv.text = it.toString()
              //  Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.getAverageIntensity().observe(viewLifecycleOwner, {
                intensityTv.text  = if(it!=null) String.format("%1.2f%%",it) else "0.0"
        })

        viewModel.getOldestRecordDate().observe(viewLifecycleOwner, {
            val s : String = it?.getStatsDateTime() ?: "---"
            oldTv.text = s
        })

        viewModel.getLatestRecordDate().observe(viewLifecycleOwner, {
            val s : String = it?.getStatsDateTime() ?: "---"
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
            //emptyTv.text = it[10].toString()

            //отобразить на donut
            val list : List<DonutSection> = listOf(
                    DonutSection(getString(R.string.dist_all_or_nothing), ContextCompat.getColor(requireContext(), R.color.colorAllOrNothing), it[0].toFloat()),
                    DonutSection(getString(R.string.dist_overgeneralizing), ContextCompat.getColor(requireContext(), R.color.colorOvergeneralizing), it[1].toFloat()),
                    DonutSection(getString(R.string.dist_filtering), ContextCompat.getColor(requireContext(), R.color.colorFiltering), it[2].toFloat()),
                    DonutSection(getString(R.string.dist_disqual_positive), ContextCompat.getColor(requireContext(), R.color.colorDisqual), it[3].toFloat()),
                    DonutSection(getString(R.string.dist_jump_conclusion), ContextCompat.getColor(requireContext(), R.color.colorJump), it[4].toFloat()),
                    DonutSection(getString(R.string.dist_magn_and_min), ContextCompat.getColor(requireContext(), R.color.colorMagnMin),it[5].toFloat()),
                    DonutSection(getString(R.string.dist_emotional_reasoning), ContextCompat.getColor(requireContext(), R.color.colorEmotional),it[6].toFloat()),
                    DonutSection(getString(R.string.dist_must_statement), ContextCompat.getColor(requireContext(), R.color.colorMust),it[7].toFloat()),
                    DonutSection(getString(R.string.dist_labeling), ContextCompat.getColor(requireContext(), R.color.colorLabeling),it[8].toFloat()),
                    DonutSection(getString(R.string.dist_personalistion), ContextCompat.getColor(requireContext(), R.color.colorPerson),it[9].toFloat()))

       //     distortionDonut.cap = it.sum().toFloat()
            distortionsDonut.submitData(list)

        })

        viewModel.getTimeOfDay()
        viewModel.timesOfDay.observe(viewLifecycleOwner,
                {
                    nightTv.text = it[0].toString()
                    morningTv.text = it[1].toString()
                    dayTv.text = it[2].toString()
                    eveningTv.text = it[3].toString()

                    val list = listOf(
                            DonutSection("Night", ContextCompat.getColor(requireContext(), R.color.colorNight),it[0].toFloat()),
                            DonutSection("Morning", ContextCompat.getColor(requireContext(), R.color.colorMorning),it[1].toFloat()),
                            DonutSection("Day", ContextCompat.getColor(requireContext(), R.color.colorDay),it[2].toFloat()),
                            DonutSection("Evening", ContextCompat.getColor(requireContext(), R.color.colorEvening),it[3].toFloat()),
                    )
                    timeDonut.submitData(list)
                })

        return ll
    }
}

class PointView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private val radius: Float
    private val customColor: Int
    private val paint: Paint

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.PointView,
                0, 0
        ).apply {
            try {
                radius = getDimension(R.styleable.PointView_pointRadius, 5F)
                customColor = getColor(R.styleable.PointView_pointColor, 0xFF0000)
            } finally {
                recycle()
            }
        }

        paint = Paint().apply {
            this.color = customColor
        }
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawCircle(width / 2F, height / 2F, radius, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension((radius*2).toInt(), (radius*2).toInt())
    }
}