package com.vva.androidopencbt.recordslist

import androidx.lifecycle.*
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class RecordListViewModel @Inject constructor(private val dataSource: RecordDao, prefs: PreferenceRepository): ViewModel() {
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val records: LiveData<List<DbRecord>> = Transformations.switchMap(prefs.isDescOrder) {
        isDesc ->

        return@switchMap if (isDesc) {
            dataSource.getAllOrdered(DbContract.ORDER_DESC)
        } else {
            dataSource.getAllOrdered(DbContract.ORDER_ASC)
        }
    }

    private var isSelectionActive = false
    private var isSelectAllActive = false

    private var _selectedItemsSet = HashSet<DbRecord>()
    val selectedItems: Set<DbRecord>
        get() = _selectedItemsSet

    fun getAllRecords() = records

    fun onItemClick(dbRecord: DbRecord): Boolean? {
        return if (!isSelectionActive) {
            null
        } else {
            isSelectAllActive = false
            if (_selectedItemsSet.contains(dbRecord)) {
                _selectedItemsSet.remove(dbRecord)
            } else {
                _selectedItemsSet.add(dbRecord)
            }

            true
        }
    }

    fun onItemLongClick(dbRecord: DbRecord) {
        if (!isSelectionActive) {
            isSelectionActive = true
            _selectedItemsSet.clear()
            onItemClick(dbRecord)
        }
    }

    fun cancelAllSelections() {
        _selectedItemsSet.clear()
        isSelectionActive = false
    }

    fun getSelectedItemsCount(): Int {
        return _selectedItemsSet.size
    }

    fun selectAll(list: List<DbRecord>) {
        isSelectAllActive = !isSelectAllActive
        if (isSelectAllActive) {
            _selectedItemsSet.addAll(list)
        } else {
            _selectedItemsSet.clear()
        }
    }

    fun deleteSelected(): Int {
        uiScope.launch(Dispatchers.IO) {
            _selectedItemsSet.forEach {
                dataSource.deleteRecord(it)
            }
        }
        return _selectedItemsSet.size
    }

    fun rollbackDeletion() {
        _selectedItemsSet.forEach {
            uiScope.launch(Dispatchers.IO) {
                dataSource.addRecord(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        job.cancel()
    }
}