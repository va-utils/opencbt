package com.vva.androidopencbt

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.vva.androidopencbt.recordslist.RvFragmentDirections

class MainActivity : AppCompatActivity() {
    private lateinit var vm: RecordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this).get(RecordsViewModel::class.java)

        vm.newRecordNavigated.observe(this, { aLong: Long ->
//            val newRecordIntent = Intent(this@MainActivity, NewRecordActivity::class.java)
//            newRecordIntent.putExtra("ID", aLong)
//            startActivity(newRecordIntent)
            findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragment().apply { recordKey = aLong })
        })
    }

    fun addNewRecord(view: View) {
//        Intent newRecordIntent = new Intent(MainActivity.this,NewRecordActivity.class);
//        startActivity(newRecordIntent);
        findNavController(R.id.myNavHostFragment).navigate(RvFragmentDirections.actionRvFragmentToDetailsFragment())
    }
}