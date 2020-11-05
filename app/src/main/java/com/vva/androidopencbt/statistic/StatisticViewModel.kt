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

    private val _distortions = MutableLiveData<IntArray>()
    val distortions: LiveData<IntArray>
        get() = _distortions

    fun getDistortionsTop()
    {
        uiScope.launch {
            withContext(Dispatchers.IO)
            {
                //сейчас будет мясо
                val list : List<Int> = db.databaseDao.getDistList()
                val servArray : IntArray = IntArray(10)
                val distArray : Array<Int> = arrayOf(0x1,0x2,0x4,0x8,0x10,0x20,0x40,0x80,0x100,0x200)
                for (n in list) //обход столбца из БД
                {
                    var i = 0
                    while(i<10) //обход каждой записи на тему искажений
                    {
                        if(((n.and(distArray[i]))!=0))
                        {
                            servArray[i]++;
                        }
                        i++;
                    }
                }
                uiScope.launch { _distortions.value = servArray }

            }
        }
    }

    fun getAllRecordsCount() = db.databaseDao.getAllCount();

    fun getAverageIntensity() = db.databaseDao.getAverageIntensity();

    fun getOldestRecordDate() = db.databaseDao.getOldestDate();

    fun getLatestRecordDate() = db.databaseDao.getLatestDate();

    override fun onCleared() {
        vmJob.cancel()
        super.onCleared()
    }
}