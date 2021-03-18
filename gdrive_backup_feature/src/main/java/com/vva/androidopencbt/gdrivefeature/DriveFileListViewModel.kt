package com.vva.androidopencbt.gdrivefeature

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DriveFileListViewModel: ViewModel() {
    private var driveServiceHelper: DriveServiceHelper? = null

    var driveClient: GoogleSignInClient? = null
    var driveAccount: GoogleSignInAccount? = null
    var driveCredentials: GoogleAccountCredential? = null
        set(value) {
            field = value
            driveServiceHelper = driveCredentials?.let {
                DriveServiceHelper.getInstance(it)
            }
        }

    private val _isLoginSuccessful = MutableLiveData<Boolean?>(null)
    val isLoginSuccessful: LiveData<Boolean?>
        get() = _isLoginSuccessful

    fun setLoginSuccessful() {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoginSuccessful.value = true
        }
    }

    fun setLoginUnsuccessful() {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoginSuccessful.value = false
        }
    }

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
            val result = withContext(Dispatchers.IO) {
                driveServiceHelper?.readFile(fileId)?.await()
            }
            _driveFile.value = result!!
        }
    }

    fun getFileList() {
        makeRequest {
            val result = withContext(Dispatchers.IO) {
                driveServiceHelper?.queryFiles()?.await()
            }

            _driveFileList.value = result?.files
        }
    }

    fun signOut() {
        driveClient?.let {
            makeRequest {
                it.signOut().await()
            }
        }
    }

    private val _requestStatus = MutableLiveData<RequestStatus?>(null)
    val requestStatus: LiveData<RequestStatus?>
        get() = _requestStatus

    private fun makeRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            _requestStatus.value = RequestStatus.InProgress
            try {
                block()
                _requestStatus.value = RequestStatus.Success
            } catch (e: Exception) {
                _requestStatus.value = RequestStatus.Failure(e)
            } finally {
                _requestStatus.value = null
            }
        }
    }
}

sealed class RequestStatus {
    object InProgress : RequestStatus()
    object Success : RequestStatus()
    class Failure(val e: Exception): RequestStatus()
}
