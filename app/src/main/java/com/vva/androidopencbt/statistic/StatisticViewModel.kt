package com.vva.androidopencbt.statistic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.RecordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(val dao: RecordDao) : ViewModel() {

//    private val db = CbdDatabase.getInstance(application)
    private var vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)

    private val _distortions = MutableLiveData<IntArray>()
    val distortions: LiveData<IntArray>
        get() = _distortions

    private val _timesOfDay = MutableLiveData<IntArray>()
    val timesOfDay: LiveData<IntArray>
        get() = _timesOfDay

    fun getTimeOfDay()
    {
        uiScope.launch {
            withContext(Dispatchers.IO) {

                val list : List<Long> = dao.getDateTimeList()
                val servArray = IntArray(4)
                for (n in list) {
                    val c : Calendar = GregorianCalendar(Locale.getDefault())
                    c.timeInMillis = n
                    when(c.get(Calendar.HOUR_OF_DAY))
                    {
                        in 0..5   ->  servArray[0]++
                        in 6..11  ->  servArray[1]++
                        in 12..17 ->  servArray[2]++
                        in 18..23 ->  servArray[3]++
                    }
                }
                uiScope.launch { _timesOfDay.value = servArray }
            }
        }
    }

    fun getDistortionsTop() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                //сейчас будет мясо
                val list : List<Int> = dao.getDistList()
                val servArray = IntArray(11)
                val distArray : Array<Int> = arrayOf(0x1,0x2,0x4,0x8,0x10,0x20,0x40,0x80,0x100,0x200)

                //обход столбца из БД
                for (n in list) {
                    var i = 0
                    //обход каждой записи на тему искажений
                    while(i<10) {
                        if(n.and(distArray[i]) != 0) {
                            servArray[i]++
                        }
                        i++
                    }
                }
                uiScope.launch { _distortions.value = servArray }

            }
        }
    }

    fun getAllRecordsCount() = dao.getAllCount()

    fun getAverageIntensity() = dao.getAverageIntensity()

    fun getOldestRecordDate() = dao.getOldestDate()

    fun getLatestRecordDate() = dao.getLatestDate()

    override fun onCleared() {
        vmJob.cancel()
        super.onCleared()
    }
}