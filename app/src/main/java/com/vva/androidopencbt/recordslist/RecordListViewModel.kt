package com.vva.androidopencbt.recordslist

import androidx.lifecycle.*
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.PreferenceRepository

class RecordListViewModel(private val dataSource: RecordDao, prefs: PreferenceRepository): ViewModel() {
    private val records: LiveData<List<DbRecord>> = Transformations.switchMap(prefs.isDescOrder) {
        isDesc ->

        return@switchMap if (isDesc) {
            dataSource.getAllOrdered(DbContract.ORDER_DESC)
        } else {
            dataSource.getAllOrdered(DbContract.ORDER_ASC)
        }
    }

    private val _isSelectionActive = MutableLiveData<Boolean>()
    val isSelectionActive: LiveData<Boolean>
        get() = _isSelectionActive

    fun getAllRecords() = records

    fun activateSelection() {
        _isSelectionActive.value = true
    }

    fun deactivateSelection() {
        _isSelectionActive.value = false
    }
}

class RecordListViewModelFactory(private val dataSource: RecordDao, private val prefs: PreferenceRepository): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordListViewModel::class.java)) {
            return RecordListViewModel(dataSource, prefs) as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}