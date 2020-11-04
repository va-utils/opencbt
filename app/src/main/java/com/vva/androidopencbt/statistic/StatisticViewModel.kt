package com.vva.androidopencbt.statistic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vva.androidopencbt.db.CbdDatabase
import kotlinx.coroutines.*

class StatisticViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CbdDatabase.getInstance(application)
    private var vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)

    fun getAllRecordsCount() = db.databaseDao.getAllCount();

    fun getAverageIntensity() = db.databaseDao.getAverageIntensity();

    fun getOldestRecordDate() = db.databaseDao.getOldestDate();

    fun getLatestRecordDate() = db.databaseDao.getLatestDate();

    override fun onCleared() {
        vmJob.cancel()
        super.onCleared()
    }
}