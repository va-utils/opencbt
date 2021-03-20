@file:Suppress("unused")

package com.vva.androidopencbt

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.db.CbdDatabase
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class RecordsViewModel(application: Application): AndroidViewModel(application) {
    private val db = CbdDatabase.getInstance(application)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    var recyclerViewState: Parcelable? = null

    private val _recordsListUpdated = MutableLiveData<Boolean>()
    val recordsListUpdated: LiveData<Boolean>
        get() = _recordsListUpdated

    private val _isAuthenticated = MutableLiveData(!prefs.getBoolean("enable_pin_protection", false))
    val isAuthenticated:LiveData<Boolean>
        get() = _isAuthenticated

    private val _askChangesConfirm = MutableLiveData<Boolean?>()
    val askDetailsFragmentConfirm: LiveData<Boolean?>
        get() = _askChangesConfirm

    private val _isSelectionActive = MutableLiveData<Boolean>()
    val isSelectionActive: LiveData<Boolean>
        get() = _isSelectionActive

    // Json string used for export in GDrive module
    var exportJsonString: String = ""

    fun askDetailsFragmentConfirmation() {
        _askChangesConfirm.value = true
    }

    fun detailsFragmentRollbackChanges() {
        _askChangesConfirm.value = false
    }

    fun detailsFragmentConfirmChangesCancel() {
        _askChangesConfirm.value = null
    }

    fun authSuccessful() {
        _isAuthenticated.value = true
    }

    fun deactivateSelection() {
        _isSelectionActive.value = false
    }

    fun activateSelection() {
        if (_isSelectionActive.value != true)
            _isSelectionActive.value = true
    }

//    private val _importInAction = MutableLiveData<Boolean?>()
//    val importInAction: LiveData<Boolean?>
//        get() = _importInAction

//    private val _importData = MutableLiveData<List<Long>?>()
//    val importData: LiveData<List<Long>?>
//        get() = _importData

    fun deleteRecord(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.databaseDao.deleteRecord(
                    db.databaseDao.getRecordById(id)
            )
        }
    }

    fun listUpdated() {
        _recordsListUpdated.value = true
        viewModelScope.launch {
            delay(TimeUnit.SECONDS.toMillis(1))
            _recordsListUpdated.value = false
        }
    }

//    private suspend fun parseJsonFile(documentUri: Uri, context: Context): List<DbRecord>? {
//        return withContext(Dispatchers.IO) {
//            val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r")
//            var list: ArrayList<DbRecord>?
//            try {
//                val string = BufferedReader(FileReader(fileDescriptor?.fileDescriptor!!)).readLine()
//                list =  Json.decodeFromString(string)
//            } catch (e: Exception) {
//                list = null
//            }
//
//            list
//        }
//    }

//    fun importRecordsFromFile(documentUri: Uri, context: Context) {
//        _importInAction.value = true
//        val ids = ArrayList<Long>()
//
//        uiScope.launch {
//            val records = parseJsonFile(documentUri, context)
//            if (records == null) {
//                _importData.value = null
//            } else {
//                withContext(Dispatchers.IO) {
//                    val currentRecords = withContext(Dispatchers.IO) {
//                        db.databaseDao.getAllList()
//                    }
//
//                    withContext(Dispatchers.Default) {
//                        for (i in records.indices) {
//                            var flag = true
//                            for (j in currentRecords.indices) {
//                                if (currentRecords[j].equalsIgnoreId(records[i])) {
//                                    flag = false
//                                    break
//                                }
//                            }
//                            if (flag)
//                                withContext(Dispatchers.IO) {
//                                    ids.add(db.databaseDao.addRecord(records[i]))
//                                }
//                        }
//                    }
//                }
//                _importData.value = ids
//            }
//
//            _importInAction.value = false
//        }
//    }

//    fun doneImporting() {
//        _importInAction.value = null
//    }


    fun restoreRecyclerView(rv: RecyclerView) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                delay(10)
            }
            rv.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }
    }
}