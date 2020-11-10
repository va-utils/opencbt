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
import com.vva.androidopencbt.BuildConfig
import com.vva.androidopencbt.R
import com.vva.androidopencbt.export.ExportViewModel
import com.vva.androidopencbt.getDateString
import java.io.File

class ExportToHtmlFragment: Fragment() {
    private lateinit var ll: LinearLayout
    private lateinit var exportBtn: Button
    private lateinit var startEditText : EditText
    private lateinit var endEditText : EditText
    private val viewModel: ExportViewModel by activityViewModels()

    private val beginDpListener = DatePickerDialog.OnDateSetListener()
    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        val d = Date(year-1900,month,dayOfMonth)
        viewModel.setBeginDate(d)
    }

    private val endDpListener = DatePickerDialog.OnDateSetListener()
    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
        val d = Date(year-1900,month,dayOfMonth)
        viewModel.setEndDate(d)
    }

    private val onClickListener = View.OnClickListener()
    {
        when(it.id)
        {
            R.id.startEditText ->
            {
                val d : Date = Date(viewModel.beginDate.value!!)
                DatePickerDialog(requireContext(),beginDpListener,d.year+1900,d.month,d.date).show()
            }
            R.id.endEditText ->
            {
                val d : Date = Date(viewModel.endDate.value!!)
                DatePickerDialog(requireContext(),endDpListener,d.year+1900,d.month,d.date).show()
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
            viewModel.makeHtmlExportFile(requireContext())
        }

        viewModel.beginDate.observe(viewLifecycleOwner, { startEditText.setText(it.getDateString()) })
        viewModel.endDate.observe(viewLifecycleOwner, { endEditText.setText(it.getDateString()) })

        viewModel.isHtmlFileReady.observe(viewLifecycleOwner, {
            if (it) {
                val file = File(requireActivity().filesDir, viewModel.htmlFileName)
                val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file)
                val forSendIntent = Intent(Intent.ACTION_SEND)
                forSendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                forSendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                forSendIntent.setDataAndType(uri, "application/html")

                val pm: PackageManager = requireActivity().packageManager
                if (forSendIntent.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(forSendIntent, getString(R.string.savehtml_text_share)))
                    viewModel.htmlFileShared()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show()
                }
            }
        })

        return ll
    }
}