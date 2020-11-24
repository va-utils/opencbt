@file:Suppress("unused")

package com.vva.androidopencbt

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.DbRecord
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception
import java.util.concurrent.TimeUnit

class RecordsViewModel(application: Application): AndroidViewModel(application) {
    private val db = CbdDatabase.getInstance(application)
    private var vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    private val _newRecordNavigated = MutableLiveData<Long>()
    val newRecordNavigated: LiveData<Long>
        get() = _newRecordNavigated

    private val _recordsListUpdated = MutableLiveData<Boolean>()
    val recordsListUpdated: LiveData<Boolean>
        get() = _recordsListUpdated

    private val _isDescOrder = MutableLiveData(prefs.getBoolean("desc_ordering", false))
    val isDescOrder: LiveData<Boolean>
        get() = _isDescOrder

    private val records: LiveData<List<DbRecord>> = Transformations.switchMap(_isDescOrder) {
        isDesc ->

        return@switchMap if (isDesc) {
            db.databaseDao.getAllOrdered(DbContract.ORDER_DESC)
        } else {
            db.databaseDao.getAllOrdered(DbContract.ORDER_ASC)
        }
    }

    private val _importInAction = MutableLiveData<Boolean?>()
    val importInAction: LiveData<Boolean?>
        get() = _importInAction

    private val _importData = MutableLiveData<List<Long>?>()
    val importData: LiveData<List<Long>?>
        get() = _importData

    fun getAllRecords() = records

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
                     intensity: Int) {
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

    fun setOrder(order: Boolean) {
        _isDescOrder.value = order
    }

    fun listUpdated() {
        _recordsListUpdated.value = true
        uiScope.launch {
            delay(TimeUnit.SECONDS.toMillis(1))
            _recordsListUpdated.value = false
        }
    }

    private suspend fun parseJsonFile(documentUri: Uri, context: Context): List<DbRecord>? {
        return withContext(Dispatchers.IO) {
            val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r")
            var list: ArrayList<DbRecord>?
            try {
                val string = BufferedReader(FileReader(fileDescriptor?.fileDescriptor!!)).readLine()
                list =  Json.decodeFromString(string)
            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Ошибка при чтении фйла", Toast.LENGTH_LONG).show()
//                }
                list = null
            }

            list
        }
    }

    fun importRecordsFromFile(documentUri: Uri, context: Context) {
        _importInAction.value = true
        val ids = ArrayList<Long>()

        uiScope.launch {
            val records = parseJsonFile(documentUri, context)
            if (records == null) {
                _importData.value = null
            } else {
                withContext(Dispatchers.IO) {
                    val currentRecords = withContext(Dispatchers.IO) {
                        db.databaseDao.getAllList()
                    }

                    withContext(Dispatchers.Default) {
                        for (i in records.indices) {
                            var flag = true
                            for (j in currentRecords.indices) {
                                if (currentRecords[j].equalsIgnoreId(records[i])) {
                                    flag = false
                                    break
                                }
                            }
                            if (flag)
                                withContext(Dispatchers.IO) {
                                    ids.add(db.databaseDao.addRecord(records[i]))
                                }
                        }
                    }
                }
                _importData.value = ids
            }

            _importInAction.value = false
        }
    }

    fun doneImporting() {
        _importInAction.value = null
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }
}