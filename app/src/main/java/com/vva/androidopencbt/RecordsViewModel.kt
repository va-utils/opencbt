package com.vva.androidopencbt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class RecordsViewModel(application: Application): AndroidViewModel(application) {
    private val db = CbdDatabase.getInstance(application)
    private var vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)

    private val _newRecordNavigated = MutableLiveData<Long>()
    val newRecordNavigated: LiveData<Long>
        get() = _newRecordNavigated

    private val _recordsListUpdated = MutableLiveData<Boolean>()
    val recordsListUpdated: LiveData<Boolean>
        get() = _recordsListUpdated

    fun getAllRecords() = db.databaseDao.getAll()

    fun getAllRecordsOrdered(order : Int) = db.databaseDao.getAllOrdered(order)

    fun getRecordById(id: Long) = db.databaseDao.getById(id)

    fun deleteRecord(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                db.databaseDao.deleteRecord(
                        db.databaseDao.getRecordById(id)
                )
            }
        }
    }

    fun addRecord(dbRecord: DbRecord) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                db.databaseDao.addRecord(dbRecord)
            }
        }
    }

    fun updateRecord(id: Long,
                     situation: String,
                     thought: String,
                     rational: String,
                     emotion: String,
                     finalDist: Int,
                     feelings: String,
                     actions: String,
                     intensity: Short) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                db.databaseDao.updateRecord(
                        DbRecord(
                                id,
                                situation,
                                thought,
                                rational,
                                emotion,
                                finalDist,
                                feelings,
                                actions,
                                intensity,
                                db.databaseDao.getRecordById(id).datetime
                        )
                )
            }
        }
    }

    fun navigateToRecord(id: Long) {
        _newRecordNavigated.value = id
    }

    fun listUpdated() {
        _recordsListUpdated.value = true
        uiScope.launch {
            delay(TimeUnit.SECONDS.toMillis(1))
            _recordsListUpdated.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }
}