package com.vva.androidopencbt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v : View = inflater.inflate(R.layout.fragment_about, container, false)

        val aboutTextView : TextView = v.findViewById(R.id.aboutTextView)
        val sendButton : Button = v.findViewById(R.id.sendButton)
        val webSiteButton : Button = v.findViewById(R.id.websiteButton)
        val cbdButton : Button = v.findViewById(R.id.cbdButton)
        val allVersionButton : Button = v.findViewById(R.id.allVersionButton)
        sendButton.setOnClickListener(listener)
        webSiteButton.setOnClickListener(listener)
        cbdButton.setOnClickListener(listener)
        allVersionButton.setOnClickListener(listener)

        aboutTextView.text = getString(R.string.app_author, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    var listener = View.OnClickListener {view -> when(view.id) {
        R.id.sendButton -> sendFeedBack()
        R.id.websiteButton -> openGitHub()
        R.id.cbdButton -> getCBTInfo()
        R.id.allVersionButton -> openGitHubAllVersions()
    }}

    private fun sendFeedBack() {
        val fbIntent = Intent(Intent.ACTION_SENDTO)
        fbIntent.data = Uri.parse("mailto:")
        fbIntent.putExtra(Intent.EXTRA_EMAIL, Array<String>(1){"androidopencbt@yandex.ru"})
        fbIntent.putExtra(Intent.EXTRA_SUBJECT, "Android OpenCBT")
        if(fbIntent.resolveActivity(requireActivity().packageManager) != null) {
            activity?.startActivity(fbIntent)
        }
    }

    private fun openGitHub() {
        //Toast.makeText(this, "Скоро эта кнопка будет открывать страницу программы на GitHub", Toast.LENGTH_SHORT).show()
        val github: Uri = Uri.parse("https://github.com/va-utils/opencbt")
        val webIntent = Intent(Intent.ACTION_VIEW, github)
        activity?.startActivity(webIntent)
    }

    private fun openGitHubAllVersions() {
        //Toast.makeText(this, "Скоро эта кнопка будет открывать страницу программы на GitHub", Toast.LENGTH_SHORT).show()
        val github: Uri = Uri.parse("https://github.com/va-utils/opencbt/releases")
        val webIntent = Intent(Intent.ACTION_VIEW, github)
        activity?.startActivity(webIntent)
    }

    private fun getCBTInfo() {
        val cbt: Uri = Uri.parse("https://github.com/va-utils/opencbt/wiki")
        val webIntent = Intent(Intent.ACTION_VIEW, cbt)
        activity?.startActivity(webIntent)
    }


}