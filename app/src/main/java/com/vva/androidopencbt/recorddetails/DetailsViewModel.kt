package com.vva.androidopencbt.recorddetails

import androidx.lifecycle.ViewModel
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao

class DetailsViewModel(
        private val recordKey: Long = 0,
        private val dataSource: RecordDao) : ViewModel() {
    fun getRecord() = dataSource.getRecordLiveDataById(recordKey)

    fun isRecordHasChanged(dbRecord1: DbRecord, dbRecord2: DbRecord): Boolean {
        return !dbRecord1.equalsIgnoreId(dbRecord2)
    }
}