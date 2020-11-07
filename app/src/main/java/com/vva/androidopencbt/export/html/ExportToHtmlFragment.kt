package com.vva.androidopencbt.export.html

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
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
import java.io.File

class ExportToHtmlFragment: Fragment() {
    private lateinit var ll: LinearLayout
    private lateinit var exportBtn: Button
    private val viewModel: ExportViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.html_export_toolbar).setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.fragment_export_html, container, false) as LinearLayout
        exportBtn = ll.findViewById(R.id.saveHTMLButton)
        exportBtn.setOnClickListener {
            viewModel.makeHtmlExportFile(requireContext())
        }

        viewModel.isHtmlExportInProgress.observe(viewLifecycleOwner, {
            if (!it) {

            }
        })

        viewModel.isHtmlFileReady.observe(viewLifecycleOwner, {
            if (it) {
                val file = File(requireActivity().filesDir, viewModel.htmlFileName)
                val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file)
                val forSendIntent = Intent(Intent.ACTION_SEND)
                forSendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                forSendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                forSendIntent.setDataAndType(uri, "application/html")
                // Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
                // Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
                val pm: PackageManager = requireActivity().packageManager
                if (forSendIntent.resolveActivity(pm) != null) {
                    startActivity(Intent.createChooser(forSendIntent, getString(R.string.savehtml_text_share)))
                } else {
                    Toast.makeText(requireContext(), getString(R.string.savehtml_error), Toast.LENGTH_SHORT).show()
                }
            }
        })

        return ll
    }
}