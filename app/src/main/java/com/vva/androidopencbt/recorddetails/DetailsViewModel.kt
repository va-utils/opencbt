package com.vva.androidopencbt.recorddetails

import androidx.lifecycle.ViewModel
import com.vva.androidopencbt.db.RecordDao

class DetailsViewModel(
        private val recordKey: Long? = null,
        dataSource: RecordDao) : ViewModel() {
    val ds = dataSource

    fun getRecord() = ds.getRecordLiveDataById(recordKey ?: 0)
}