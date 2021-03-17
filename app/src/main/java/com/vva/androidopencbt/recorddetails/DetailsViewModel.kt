package com.vva.androidopencbt.recorddetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import kotlinx.coroutines.*

class DetailsViewModel(
        private val recordKey: Long = 0,
        private val dataSource: RecordDao) : ViewModel() {
    private val vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)
    var currentRecord: DbRecord? = null

    fun getRecord() = dataSource.getRecordLiveDataById(recordKey)

    fun isRecordHasChanged(dbRecord: DbRecord): Boolean {
        return currentRecord?.let {
            !dbRecord.equalsIgnoreIdAndDate(it)
        } ?: false
    }

    fun deleteRecordById(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                dataSource.deleteRecord(
                        dataSource.getRecordById(id)
                )
            }
        }
    }

    fun addRecord(dbRecord: DbRecord) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                dataSource.addRecord(dbRecord)
            }
        }
    }

    fun updateRecord(
            id: Long,
            situation: String,
            thought: String,
            rational: String,
            emotion: String,
            finalDist: Int,
            feelings: String,
            actions: String,
            intensity: Int
    ) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                dataSource.updateRecord(
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
                                dataSource.getRecordById(id).datetime
                        )
                )
            }
        }
    }
}

class DetailsViewModelFactory(
        private val recordKey: Long,
        private val dataSource: RecordDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(recordKey, dataSource) as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}