package com.vva.androidopencbt.recordslist

import androidx.lifecycle.*
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.PreferenceRepository
import kotlinx.coroutines.*

class RecordListViewModel(private val dataSource: RecordDao, prefs: PreferenceRepository): ViewModel() {
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

    private var _selectedItemsArray = HashMap<Long, Boolean>()

    private val _selectedItems = MutableLiveData<HashMap<Long, Boolean>>()
    val selectedItems: LiveData<HashMap<Long, Boolean>>
        get() = _selectedItems

    fun getAllRecords() = records

    fun onItemClick(dbRecord: DbRecord): Boolean? {
        return if (!isSelectionActive) {
            null
        } else {
            isSelectAllActive = false
            val isSelected = _selectedItemsArray[dbRecord.id] ?: false
            _selectedItemsArray[dbRecord.id] = !isSelected
            _selectedItems.value = _selectedItemsArray
            true
        }
    }

    fun onItemLongClick(dbRecord: DbRecord) {
        if (!isSelectionActive) {
            isSelectionActive = true
            onItemClick(dbRecord)
        }
    }

    fun cancelAllSelections() {
        _selectedItemsArray = HashMap()
        _selectedItems.value = _selectedItemsArray
        isSelectionActive = false
    }

    fun selectAll(list: List<DbRecord>) {
        isSelectAllActive = !isSelectAllActive
        if (isSelectAllActive) {
            list.forEach {
                _selectedItemsArray[it.id] = true
                _selectedItems.value = _selectedItemsArray
            }
        } else {
            _selectedItemsArray = HashMap()
            _selectedItems.value = _selectedItemsArray
        }
    }

    fun deleteSelected() {
        _selectedItemsArray.filterValues {
            true
        }.forEach {
            uiScope.launch(Dispatchers.IO) {
                dataSource.deleteById(it.key)
            }
            _selectedItemsArray.remove(it.key)
        }
    }

    override fun onCleared() {
        super.onCleared()

        job.cancel()
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