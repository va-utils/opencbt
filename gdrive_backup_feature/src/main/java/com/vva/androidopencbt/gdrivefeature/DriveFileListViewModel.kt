package com.vva.androidopencbt.gdrivefeature

import androidx.lifecycle.*
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DriveFileListViewModel: ViewModel() {
    var driveServiceHelper: DriveServiceHelper? = null
        set(value) {
            field = value
            refreshFileList()
        }

    private val _isLogInSuccessful = MutableLiveData<Boolean?>(null)
    val isLogInSuccessful: LiveData<Boolean?>
        get() = _isLogInSuccessful

    fun setLogInSuccessful() {
        _isLogInSuccessful.value = true
    }

    fun setLogInUnsuccessful() {
        _isLogInSuccessful.value = false
    }

    private val _isRequestIsActive = MutableLiveData<Boolean>()
    val isRequestIsActive: LiveData<Boolean>
        get() = _isRequestIsActive

    private val _driveFileList = MutableLiveData<List<File>>()
    val driveFileList: LiveData<List<File>>
        get() = _driveFileList

    private val _driveFile = MutableLiveData<Pair<String, String>>()
    val driveFile: LiveData<Pair<String, String>>
        get() = _driveFile

    fun refreshFileList() {
        makeRequest {
            withContext(Dispatchers.IO) {
                val result = driveServiceHelper?.queryFiles()?.await()
                withContext(Dispatchers.Main) {
                    _driveFileList.value = result?.files
                }
            }
        }
    }

    fun getFile(fileId: String) {
        makeRequest {
            withContext(Dispatchers.IO) {
                val result = driveServiceHelper?.readFile(fileId)?.await()
                withContext(Dispatchers.Main) {
                    _driveFile.value = result!!
                }
            }
        }
    }

    private fun makeRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isRequestIsActive.value = true
            block()
            _isRequestIsActive.value = false
        }
    }
}
