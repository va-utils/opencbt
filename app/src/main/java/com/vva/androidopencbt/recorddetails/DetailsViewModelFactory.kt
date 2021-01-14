package com.vva.androidopencbt.recorddetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vva.androidopencbt.db.RecordDao

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