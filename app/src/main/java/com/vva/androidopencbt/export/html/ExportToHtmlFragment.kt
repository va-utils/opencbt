package com.vva.androidopencbt.export.html

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.BuildConfig
import com.vva.androidopencbt.R
import com.vva.androidopencbt.export.ExportViewModel
import com.vva.androidopencbt.getDateString
import org.joda.time.DateTime
import java.io.File

class ExportToHtmlFragment: Fragment() {
    private lateinit var ll: LinearLayout
    private lateinit var exportBtn: Button
    private lateinit var startEditText : EditText
    private lateinit var endEditText : EditText
    private lateinit var htmlRb : RadioButton
    private lateinit var jsonRb : RadioButton
    private val exportViewModel: ExportViewModel by activityViewModels()

    private val beginDpListener = DatePickerDialog.OnDateSetListener {
        _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        exportViewModel.setBeginDate(DateTime(year, month+1, dayOfMonth, 0, 0))
    }

    private val endDpListener = DatePickerDialog.OnDateSetListener {
        _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        exportViewModel.setEndDate(DateTime(year, month+1, dayOfMonth, 23, 59))
    }

    private val onClickListener = View.OnClickListener() {
        when(it.id) {
            R.id.startEditText -> {
                val date = exportViewModel.beginDate.value!!
                DatePickerDialog(requireContext(), beginDpListener, date.year, date.monthOfYear-1, date.dayOfMonth).show()
            }

            R.id.endEditText -> {
                val date = exportViewModel.endDate.value!!
                DatePickerDialog(requireContext(), endDpListener, date.year, date.monthOfYear-1, date.dayOfMonth).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.html_export_toolbar).setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.fragment_export_html, container, false) as LinearLayout

        //---для выбора периода
        startEditText = ll.findViewById(R.id.startEditText)
        endEditText = ll.findViewById(R.id.endEditText)
        startEditText.setOnClickListener(onClickListener)
        endEditText.setOnClickListener(onClickListener)
        //--
        exportBtn = ll.findViewById(R.id.saveHTMLButton)
        exportBtn.setOnClickListener {
            exportViewModel.makeExportFile(requireContext())
        }

        htmlRb = ll.findViewById(R.id.htmlRb)
        jsonRb = ll.findViewById(R.id.jsonRb)

        exportViewModel.getDefaultFormat(requireContext()).observe(viewLifecycleOwner,
                {
                    when(it)
                    {
                        "JSON" -> jsonRb.isChecked = true
                        "HTML" -> htmlRb.isChecked = true
                    }
                })

        exportViewModel.beginDate.observe(viewLifecycleOwner, { startEditText.setText(it.getDateString()) })
        exportViewModel.endDate.observe(viewLifecycleOwner, { endEditText.setText(it.getDateString()) })

        exportViewModel.isHtmlExportInProgress.observe(viewLifecycleOwner, {

        })

        exportViewModel.isHtmlFileReady.observe(viewLifecycleOwner, {
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val fileType = when (prefs.getString("default_export", "HTML")) {
                "JSON" -> {
                   "application/json"
                }
                "HTML" -> {
                    "application/html"
                }
                else -> {
                    throw IllegalArgumentException("No such format")
                }
            }
            if (it) {
                val file = File(requireActivity().filesDir, exportViewModel.fileName)
                val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file)
                val forSendIntent = Intent(Intent.ACTION_SEND)
                forSendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                forSendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                forSendIntent.setDataAndType(uri, fileType)

                val pm: PackageManager = requireActivity().packageManager
                if (forSendIntent.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(forSendIntent, getString(R.string.savehtml_text_share)))
                    exportViewModel.htmlFileShared()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show()
                }
            }
        })

        return ll
    }


}