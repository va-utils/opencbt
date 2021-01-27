package com.vva.androidopencbt.recordslist

import androidx.lifecycle.*
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.PreferenceRepository
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

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

    private var _selectedItemsMap = HashMap<Long, Boolean>()
    private var _selectedItemsCache = ArrayList<DbRecord>()

    private val _selectedItems = MutableLiveData<HashMap<Long, Boolean>>()
    val selectedItems: LiveData<HashMap<Long, Boolean>>
        get() = _selectedItems

    fun getAllRecords() = records

    fun onItemClick(dbRecord: DbRecord): Boolean? {
        return if (!isSelectionActive) {
            null
        } else {
            isSelectAllActive = false
            val isSelected = _selectedItemsMap[dbRecord.id] ?: false
            if (!isSelected)
                _selectedItemsCache.add(dbRecord)
            else
                _selectedItemsCache.remove(dbRecord)
            _selectedItemsMap[dbRecord.id] = !isSelected
            _selectedItems.value = _selectedItemsMap
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
        _selectedItemsMap = HashMap()
        _selectedItems.value = _selectedItemsMap
        isSelectionActive = false

        uiScope.launch(Dispatchers.Default) {
            delay(TimeUnit.SECONDS.toMillis(DELETE_HOLD_TIME_SEC))
            _selectedItemsCache = ArrayList()
        }
    }

    fun selectAll(list: List<DbRecord>) {
        isSelectAllActive = !isSelectAllActive
        if (isSelectAllActive) {
            list.forEach {
                _selectedItemsMap[it.id] = true
                _selectedItems.value = _selectedItemsMap
            }
        } else {
            _selectedItemsMap = HashMap()
            _selectedItems.value = _selectedItemsMap
        }
    }

    fun deleteSelected(): List<DbRecord> {
        _selectedItemsMap.filterValues {
            it
        }.forEach {
            uiScope.launch(Dispatchers.IO) {
                dataSource.deleteById(it.key)
            }
            _selectedItemsMap.remove(it.key)
        }
        return _selectedItemsCache
    }

    fun rollbackDeletion() {
        _selectedItemsCache.forEach {
            uiScope.launch(Dispatchers.IO) {
                dataSource.addRecord(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        job.cancel()
    }

    companion object {
        const val DELETE_HOLD_TIME_SEC = 5L
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