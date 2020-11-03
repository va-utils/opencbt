package com.vva.androidopencbt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vva.androidopencbt.db.CbdDatabase

class StatisticViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CbdDatabase.getInstance(application)

    fun getAllRecordsCount() = db.databaseDao.getAllCount()

    fun getAverageIntensity() = db.databaseDao.getAverageIntensity()

    override fun onCleared() {
        super.onCleared()
    }
}