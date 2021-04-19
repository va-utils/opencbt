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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vva.androidopencbt.*
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.ExportFormats
import com.vva.androidopencbt.settings.PreferenceRepository
import org.joda.time.DateTime
import java.io.File

class ExportFragment: Fragment() {
    private lateinit var ll: LinearLayout
    private lateinit var beginDate: EditText
    private lateinit var endDate: EditText
    private lateinit var wholeDiary: CheckBox
    private lateinit var formatGroup: RadioGroup
    private lateinit var goBtn: Button
    private lateinit var cloudGoBtn: Button

    private lateinit var args: ExportFragmentArgs

    private lateinit var dao: RecordDao
    private val exportViewModel: ExportViewModel by activityViewModels()
    private lateinit var prefs: PreferenceRepository

    private val beginDpListener = DatePickerDialog.OnDateSetListener {
        _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        exportViewModel.setBeginDate(DateTime(year, month+1, dayOfMonth, 0, 0))
    }

    private val endDpListener = DatePickerDialog.OnDateSetListener {
        _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        exportViewModel.setEndDate(DateTime(year, month+1, dayOfMonth, 23, 59))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.export_wizard, container, false) as LinearLayout
        prefs = (requireActivity().application as App).preferenceRepository

        initViews()
        initDateEditText()
        initExportButtons()

        args = ExportFragmentArgs.fromBundle(requireArguments())

        wholeDiary.setOnCheckedChangeListener { _, b ->
            beginDate.isEnabled = !b
            endDate.isEnabled = !b
            ll.findViewById<TextView>(R.id.endDate_tv).isEnabled = !b
            ll.findViewById<TextView>(R.id.beginDate_tv).isEnabled = !b
        }
        wholeDiary.isChecked = true

        formatGroup = ll.findViewById(R.id.radioGroup)
        when (args.format) {
            Export.FORMAT_PICK -> {
                formatGroup.visibility = View.VISIBLE
                when (prefs.defaultExportFormat.value) {
                    ExportFormats.CSV -> {
                        formatGroup.check(R.id.csv_rb)
                    }
                    ExportFormats.HTML -> {
                        formatGroup.check(R.id.html_rb)
                    }
                    else -> {
                        formatGroup.check(R.id.csv_rb)
                    }
                }
            }
            else -> {
                formatGroup.visibility = View.GONE
            }
        }

        val alertDialog: AlertDialog = makeIndeterminateProgressDialog()
        exportViewModel.exportState.observe(viewLifecycleOwner) {
            when(it) {
                is ExportStates.InProgress -> {
                    alertDialog.show()
                }
                is ExportStates.Success -> {
                    alertDialog.dismiss()

                    when (it.isCloud) {
                        false -> {
                            sendLocalFile(it.filePath)
                            findNavController().popBackStack()
                        }
                        true -> {
                            sendCloud(it.fileName, it.filePath)
                        }
                    }
                }
                is ExportStates.Failure -> {
                    alertDialog.dismiss()
                    Log.d("Export", "error", it.e)
                    Toast.makeText(requireContext(), "Что-то пошло не так, выгрузка не удалась", Toast.LENGTH_LONG).show()
                }
                null -> {
                    alertDialog.dismiss()
                }
            }
        }

        return ll
    }

    private fun sendCloud(fileName: String, filePath: String) {
        findNavController()
                .navigate(NavigationDirections.actionGlobalDriveListFragment(fileName, filePath))
    }

    private fun sendLocalFile(filePath: String) {
        val file = File(filePath)
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

    private fun makeIndeterminateProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setBackground(ResourcesCompat.getDrawable(resources, R.drawable.pg_back, null))
                .setView(R.layout.progress_bar)
                .setCancelable(false)
                .create()
    }

    private fun initDateEditText() {
        beginDate = ll.findViewById(R.id.startDate_et)
        endDate = ll.findViewById(R.id.endDate_et)

        exportViewModel.beginDate.observe(viewLifecycleOwner) {
            beginDate.setText(it.getDateString())
        }
        exportViewModel.endDate.observe(viewLifecycleOwner) {
            endDate.setText(it.getDateString())
        }

        beginDate.setOnClickListener {
            val date = exportViewModel.beginDateTime
            DatePickerDialog(requireContext(), beginDpListener, date.year, date.monthOfYear-1, date.dayOfMonth).show()
        }
        endDate.setOnClickListener {
            val date = exportViewModel.endDateTime
            DatePickerDialog(requireContext(), endDpListener, date.year, date.monthOfYear-1, date.dayOfMonth).show()
        }
    }

    private fun getPickedFormat(): ExportFormats {
        return when (formatGroup.checkedRadioButtonId) {
            R.id.html_rb -> {
                ExportFormats.HTML
            }
            R.id.csv_rb -> {
                ExportFormats.CSV
            }
            else -> {
                throw IllegalStateException("No such format")
            }
        }
    }

    private fun initExportButtons() {
        goBtn.setOnClickListener {
            val exportBuilder = Export.Builder()

            when (args.format) {
                Export.FORMAT_JSON -> {
                    exportBuilder.setFormat(ExportFormats.JSON)
                }
                Export.FORMAT_HTML -> {
                    exportBuilder.setFormat(ExportFormats.HTML)
                }
                Export.FORMAT_CSV -> {
                    exportBuilder.setFormat(ExportFormats.CSV)
                }
                Export.FORMAT_PICK -> {
                    exportBuilder.setFormat(getPickedFormat())
                }
            }
            when (args.destination) {
                Export.DESTINATION_CLOUD -> {
                    exportBuilder.cloud()
                }
                else -> {
                }
            }
            if (wholeDiary.isChecked) {
                exportBuilder
                        .setFileName("CBT_diary")
            } else {
                exportBuilder
                        .setFileName("CBT_diary_${exportViewModel.beginDateTime.toString("dd-MM-yyy")}_${exportViewModel.endDateTime.toString("dd-MM-yyyy")}")
                        .setPeriod(exportViewModel.beginDateTime, exportViewModel.endDateTime)
            }

            exportViewModel.export(exportBuilder.build())
        }

        cloudGoBtn.setOnClickListener {
            val exportBuilder = Export.Builder()
                    .cloud()

            when (args.format) {
                Export.FORMAT_JSON -> {
                    exportBuilder.setFormat(ExportFormats.JSON)
                }
                Export.FORMAT_HTML -> {
                    exportBuilder.setFormat(ExportFormats.HTML)
                }
                Export.FORMAT_CSV -> {
                    exportBuilder.setFormat(ExportFormats.CSV)
                }
                Export.FORMAT_PICK -> {
                    exportBuilder.setFormat(getPickedFormat())
                }
            }

            if (wholeDiary.isChecked) {
                exportBuilder
                        .setFileName("CBT_diary")
            } else {
                exportBuilder
                        .setFileName("CBT_diary_${exportViewModel.beginDateTime.toString("dd-MM-yyy")}_${exportViewModel.endDateTime.toString("dd-MM-yyyy")}")
                        .setPeriod(exportViewModel.beginDateTime, exportViewModel.endDateTime)
            }

            exportViewModel.export(exportBuilder.build())
        }
    }

    private fun initViews() {
        dao = CbdDatabase.getInstance(requireContext()).databaseDao

        wholeDiary = ll.findViewById(R.id.whole_cb)
        goBtn = ll.findViewById(R.id.exportBtn)
        cloudGoBtn = ll.findViewById(R.id.cloudExportBtn)

        prefs.isDriveIntegrationEnabled.observe(viewLifecycleOwner) {
            if (!it || args.destination == Export.DESTINATION_CLOUD) {
                cloudGoBtn.visibility = View.GONE
            }
        }
    }
}