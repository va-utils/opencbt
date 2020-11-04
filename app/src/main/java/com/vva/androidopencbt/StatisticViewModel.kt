package com.vva.androidopencbt

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

    private val _recordListCounted = MutableLiveData<Int>()
    val recordListCounted : LiveData<Int>
    get() { return _recordListCounted}

    private val _averageIntensityCalculated = MutableLiveData<Double>()
    val averageIntesityCalculated : LiveData<Double>
    get() { return _averageIntensityCalculated}


    fun getAllRecordsCount() = db.databaseDao.getAllCount();

    fun getAverageIntensity() = db.databaseDao.getAverageIntensity();

    override fun onCleared() {
        super.onCleared()
    }
}