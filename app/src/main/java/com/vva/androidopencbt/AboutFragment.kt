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

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val v : View = inflater.inflate(R.layout.fragment_about, container, false)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toolbar = requireActivity().findViewById(R.id.toolbar) as Toolbar
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        val versionTextView : TextView = v.findViewById(R.id.versionTextView)
        val sendButton : Button = v.findViewById(R.id.sendButton)
        val webSiteButton : Button = v.findViewById(R.id.websiteButton)
        val cbdButton : Button = v.findViewById(R.id.cbdButton)
        sendButton.setOnClickListener(listener)
        webSiteButton.setOnClickListener(listener)
        cbdButton.setOnClickListener(listener)
        versionTextView.text = getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu()
    }

    var listener = View.OnClickListener {view -> when(view.id) {
        R.id.sendButton -> sendFeedBack()
        R.id.websiteButton -> openGitHub()
        R.id.cbdButton -> getCBTInfo()
    }}

    private fun sendFeedBack() {
        val fbIntent = Intent(Intent.ACTION_SENDTO)
        fbIntent.data = Uri.parse("mailto:")
        fbIntent.putExtra(Intent.EXTRA_EMAIL, Array<String>(1){"androidopencbt@yandex.ru"})
        fbIntent.putExtra(Intent.EXTRA_SUBJECT, "Android OpenCBT")
        if(fbIntent.resolveActivity((requireActivity().packageManager))!=null) {
            activity?.startActivity(fbIntent)
        }
    }

    private fun openGitHub() {
        //Toast.makeText(this, "Скоро эта кнопка будет открывать страницу программы на GitHub", Toast.LENGTH_SHORT).show()
        val github: Uri = Uri.parse("https://github.com/va-utils/opencbt")
        val webIntent: Intent = Intent(Intent.ACTION_VIEW, github)
        activity?.startActivity(webIntent)
    }

    private fun getCBTInfo() {
        val cbt: Uri = Uri.parse("https://github.com/va-utils/opencbt/wiki")
        val webIntent: Intent = Intent(Intent.ACTION_VIEW, cbt)
        activity?.startActivity(webIntent)
    }


}