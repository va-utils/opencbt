package com.vva.androidopencbt.statistic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
    private lateinit var sv : ScrollView
    private val viewModel: StatisticViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        sv = inflater.inflate(R.layout.fragment_statistic, container, false) as ScrollView

        totalTv = sv.findViewById(R.id.totalTv)
        oldTv = sv.findViewById(R.id.oldTv)
        latestTv = sv.findViewById(R.id.latestTv)

        timeDonut = sv.findViewById(R.id.timeDonut)
        nightTv = sv.findViewById(R.id.nightTv)
        morningTv = sv.findViewById(R.id.morningTv)
        dayTv = sv.findViewById(R.id.dayTv)
        eveningTv = sv.findViewById(R.id.eveningTv)

        distortionsDonut = sv.findViewById(R.id.distortionsDonut)
        allOrNothingTv = sv.findViewById(R.id.allOrNothingTv)
        overgeneralizingTv = sv.findViewById(R.id.overgeneralizingTv)
        filteringTv = sv.findViewById(R.id.filteringTv)
        disqualTv = sv.findViewById(R.id.disqualTv)
        jumpTv = sv.findViewById(R.id.jumpTv)
        magnAndMinTv = sv.findViewById(R.id.magnMinTv)
        emotionalTv = sv.findViewById(R.id.emoReasonTv)
        mustTv = sv.findViewById(R.id.mustTv)
        labelingTv = sv.findViewById(R.id.labelingTv)
        personTv = sv.findViewById(R.id.personTv)
     //   emptyTv = ll.findViewById(R.id.emptyTv)

        intensityTv = sv.findViewById(R.id.intensityTv)


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
            val s = if(it.millis!=0L)
                it.getStatsDateTime()
            else
                "---"
            oldTv.text = s
        })

        viewModel.getLatestRecordDate().observe(viewLifecycleOwner, {
            val s = if(it.millis!=0L)
                it.getStatsDateTime()
            else
                "---"
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
            distortionsDonut.submitData(list.sortedBy { it.amount })

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
                    timeDonut.submitData(list.sortedBy { it.amount })
                })

        return sv
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