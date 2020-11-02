package com.vva.androidopencbt.recordslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.*

class RvFragment: Fragment() {
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var ll: ConstraintLayout
    private lateinit var rv: RecyclerView
    private lateinit var dataAdapter: RecordsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.rv_layout, container, false) as ConstraintLayout
        rv = ll.findViewById(R.id.rv)
        dataAdapter = RecordsAdapter(RecordListener {
            it?.let {
                viewModel.navigateToRecord(it.id ?: 0)
            }
        })
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val orderBy = if (prefs.getBoolean("desc_ordering", true)) 0 else 1
        viewModel.getAllRecordsOrdered(orderBy).observe(viewLifecycleOwner, {
            dataAdapter.submitList(it)
        })
        rv.adapter = dataAdapter
        
        return ll
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AboutFragment())
                    .addToBackStack("test")
                    .commit()
            return true
        }

        if (id == R.id.action_html) {
            val pdfIntent = Intent(requireContext(), SaveHTMLActivity::class.java)
            startActivity(pdfIntent)
            return true
        }

        if (id == R.id.action_settings) {
            val settingsIntent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(settingsIntent)
            return true
        }

        if (id == R.id.action_newrecord) {
            val newRecordIntent = Intent(requireContext(), NewRecordActivity::class.java)
            startActivity(newRecordIntent)
        }

        return super.onOptionsItemSelected(item)
    }
}