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
class RecordListViewModel @Inject constructor(private val dataSource: RecordDao, private val prefs: PreferenceRepository): ViewModel() {
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

    private var _selectedItemsMap = HashMap<DbRecord, Boolean>()
    private var deletedCache = ArrayList<DbRecord>()

    private val _selectedItems = MutableLiveData<HashMap<DbRecord, Boolean>>()
    val selectedItems: LiveData<HashMap<DbRecord, Boolean>>
        get() = _selectedItems

    fun getAllRecords() = records

    fun onItemClick(dbRecord: DbRecord): Boolean? {
        return if (!isSelectionActive) {
            null
        } else {
            isSelectAllActive = false
            _selectedItemsMap[dbRecord] = _selectedItemsMap[dbRecord] != true
            _selectedItems.value = _selectedItemsMap
            true
        }
    }

    fun onItemLongClick(dbRecord: DbRecord) {
        if (!isSelectionActive) {
            isSelectionActive = true
            deletedCache.clear()
            _selectedItemsMap.clear()
            _selectedItems.value = _selectedItemsMap
            onItemClick(dbRecord)
        }
    }

    fun cancelAllSelections() {
        _selectedItemsMap.clear()
        _selectedItems.value = _selectedItemsMap
        isSelectionActive = false
    }

    fun selectAll(list: List<DbRecord>) {
        isSelectAllActive = !isSelectAllActive
        if (isSelectAllActive) {
            list.forEach {
                _selectedItemsMap[it] = true
                _selectedItems.value = _selectedItemsMap
            }
        } else {
            _selectedItemsMap.clear()
            _selectedItems.value = _selectedItemsMap
        }
    }

    fun deleteSelected(): Int {
        val selected = _selectedItemsMap.filterValues {
            it
        }
        deletedCache.addAll(selected.keys)
        uiScope.launch(Dispatchers.IO) {
            selected.forEach {
                dataSource.deleteRecord(it.key)
            }
        }
        return selected.size
    }

    fun rollbackDeletion() {
        deletedCache.forEach {
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

//class RecordListViewModelFactory(private val dataSource: RecordDao, private val prefs: PreferenceRepository): ViewModelProvider.Factory {
//    @Suppress("unchecked_cast")
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(RecordListViewModel::class.java)) {
//            return RecordListViewModel(dataSource, prefs) as T
//        }
//        throw IllegalAccessException("Unknown ViewModel class")
//    }
//}