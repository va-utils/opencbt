package com.vva.androidopencbt.import

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception

class ImportViewModel(private val dao: RecordDao): ViewModel() {
    private val _isImportInAction = MutableLiveData<Boolean>()
    val isImportInAction: LiveData<Boolean>
        get() = _isImportInAction

    private var importedRecordIds: List<Long>? = null

    fun importRecordsFromFile(docUri: Uri, context: Context) {
        import {
            val string = getStringFromFile(docUri, context)
            if (string == null) {
                importedRecordIds = null
            } else {
                val list = parseJson(string)
                val currentList = withContext(Dispatchers.IO){
                    dao.getAllList()
                }
                val listToImport = list.filter { parsed ->
                    var flag = true
                    currentList.forEach {
                        if (parsed.equalsIgnoreId(it)) {
                            flag = false
                            return@forEach
                        }
                    }
                    flag
                }
                importedRecordIds = listToImport.map {
                    it.id
                }
                withContext(Dispatchers.IO) {
                    listToImport.forEach {
                        dao.addRecord(it)
                    }
                }
            }
        }
    }

    fun rollbackLastImport() {
        importedRecordIds?.let { list ->
            viewModelScope.launch(Dispatchers.IO) {
                list.forEach {
                    dao.deleteById(it)
                }
            }
        }
    }

    private fun parseJson(json: String): List<DbRecord> {
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getStringFromFile(docUri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openFileDescriptor(docUri, "r")?.use {
                BufferedReader(FileReader(it.fileDescriptor)).readLine()
            }
        }
    }

    private fun import(block: suspend () -> Unit) {
        _isImportInAction.value = true
        viewModelScope.launch {
            block()
        }
        _isImportInAction.value = false
    }

}

class ImportViewModelFactory(private val dao: RecordDao): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            return ImportViewModel(dao) as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}