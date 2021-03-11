package com.vva.androidopencbt.export

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vva.androidopencbt.*
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.ExportFormats
import org.joda.time.DateTime
import java.io.File

class ExportFragment: Fragment() {
    private lateinit var ll: LinearLayout
    private lateinit var beginDate: EditText
    private lateinit var endDate: EditText
    private lateinit var wholeDiary: CheckBox
    private lateinit var formatGroup: RadioGroup
    private lateinit var goBtn: Button

    private lateinit var dao: RecordDao
    private val exportViewModelNew: ExportViewModelNew by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private val beginDpListener = DatePickerDialog.OnDateSetListener {
        _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        exportViewModelNew.setBeginDate(DateTime(year, month+1, dayOfMonth, 0, 0))
    }

    private val endDpListener = DatePickerDialog.OnDateSetListener {
        _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        exportViewModelNew.setEndDate(DateTime(year, month+1, dayOfMonth, 23, 59))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dao = CbdDatabase.getInstance(requireContext()).databaseDao

        ll = inflater.inflate(R.layout.wizard_pager2, container, false) as LinearLayout
        wholeDiary = ll.findViewById(R.id.whole_cb)
        goBtn = ll.findViewById(R.id.exportBtn)

        initDateEditText()

        wholeDiary.setOnCheckedChangeListener { _, b ->
            beginDate.isEnabled = !b
            endDate.isEnabled = !b
            ll.findViewById<TextView>(R.id.endDate_tv).isEnabled = !b
            ll.findViewById<TextView>(R.id.beginDate_tv).isEnabled = !b
        }

        formatGroup = ll.findViewById(R.id.radioGroup)
        formatGroup.visibility = View.GONE
        val args = ExportFragmentArgs.fromBundle(requireArguments())

        goBtn.setOnClickListener {
            val format = when (args.format) {
                0 -> {
                    ExportFormats.JSON
                }
                else -> {
                    ExportFormats.HTML
                }
            }
            val export = if (wholeDiary.isChecked) {
                Export.Builder()
                        .setFileName("CBT_diary")
                        .setFormat(format)
                        .build()
            } else {
                Export.Builder()
                        .setFileName("CBT_diary_${exportViewModelNew.beginDateTime.toString("dd-MM-yyy")}_${exportViewModelNew.endDateTime.toString("dd-MM-yyyy")}")
                        .setFormat(format)
                        .setPeriod(exportViewModelNew.beginDateTime, exportViewModelNew.endDateTime)
                        .build()
            }

            exportViewModelNew.export(export)
        }

        exportViewModelNew.exportState.observe(viewLifecycleOwner) {
            when(it) {
                is ProcessStates.InProgress -> {
                    ll.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                    ll.findViewById<ConstraintLayout>(R.id.cl).isEnabled = false
                }
                is ProcessStates.Success -> {
                    ll.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    ll.findViewById<ConstraintLayout>(R.id.cl).isEnabled = true

                    val file = File(requireActivity().filesDir, exportViewModelNew.fileName)
                    val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file)
                    val forSendIntent = Intent(Intent.ACTION_SEND)
                    forSendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    forSendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    forSendIntent.setDataAndType(uri, "application/octet-stream")

                    val pm: PackageManager = requireActivity().packageManager
                    if (forSendIntent.resolveActivity(pm) != null) {
                        startActivity(Intent.createChooser(forSendIntent, getString(R.string.savehtml_text_share)))
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show()
                    }

                }
                is ProcessStates.Failure -> {
                    Log.d("Export", "error", it.e)
                    Toast.makeText(requireContext(), "Что-то пошло не так, выгрузка не удалась", Toast.LENGTH_LONG).show()
                }
                null -> {
                    ll.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    ll.findViewById<ConstraintLayout>(R.id.cl).isEnabled = true
                }
            }
        }

        return ll
    }

    private fun initDateEditText() {
        beginDate = ll.findViewById(R.id.startDate_et)
        endDate = ll.findViewById(R.id.endDate_et)

        exportViewModelNew.beginDate.observe(viewLifecycleOwner) {
            beginDate.setText(it.getDateString())
        }
        exportViewModelNew.endDate.observe(viewLifecycleOwner) {
            endDate.setText(it.getDateString())
        }

        beginDate.setOnClickListener {
            val date = exportViewModelNew.beginDateTime
            DatePickerDialog(requireContext(), beginDpListener, date.year, date.monthOfYear-1, date.dayOfMonth).show()
        }
        endDate.setOnClickListener {
            val date = exportViewModelNew.endDateTime
            DatePickerDialog(requireContext(), endDpListener, date.year, date.monthOfYear-1, date.dayOfMonth).show()
        }
    }
}