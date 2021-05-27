package com.vva.androidopencbt.gdrivefeature

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.model.File
import com.vva.androidopencbt.db.DbRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class DriveFileListViewModel @Inject constructor(): ViewModel() {
    private val tagLog = javaClass.canonicalName
    var appDirId = ""

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

    private val _driveFileList = MutableLiveData<List<File>>()
    val driveFileList: LiveData<List<File>>
        get() = _driveFileList

    private val _driveFile = MutableLiveData<Pair<String, String>>()
    val driveFile: LiveData<Pair<String, String>>
        get() = _driveFile

    fun refreshFileList() {
        makeRequest {
            withContext(Dispatchers.IO) {
                val result = driveServiceHelper?.queryFiles(appDirId)?.await()
                withContext(Dispatchers.Main) {
                    _driveFileList.value = result?.files
                }
            }
        }
    }

    suspend fun readFile(file: File): List<DbRecord>? {
        val result = withContext(Dispatchers.IO) {
            driveServiceHelper?.readFile(file.id)?.await()
        }

        return result?.second?.let {
            Log.d("IMPORT", it)
                val list: List<DbRecord> = Json.decodeFromString(it)
                Log.d("IMPORT", list.toString())
                list
        }
    }

    fun getFileList() {
        makeRequest {
            val result = withContext(Dispatchers.IO) {
                driveServiceHelper?.queryFiles(appDirId)?.await()
            }

            _driveFileList.value = result?.files
        }
    }

    fun uploadFileAppRoot(fileName: String, filePath: String) {
        driveServiceHelper?.let {
            makeBlockingRequest {
                it.uploadFile(listOf(appDirId), fileName, filePath)
            }
        }
    }

    fun signOut() {
        driveClient?.let {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    it.signOut().await()
                    withContext(Dispatchers.Main) {
                        clearCredentials()
                    }

                } catch (e: Exception) {
                    Log.e(tagLog, "exception", e)
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return driveAccount != null
                && driveClient != null
                && driveCredentials != null
                && driveAccount?.isExpired != true
    }

    private suspend fun checkAndMakeRootFolder() {
        driveServiceHelper?.let {
            withContext(Dispatchers.IO) {
                val result = it.checkFolderExist(ROOT_FOLDER).await()
                if (result.files.isEmpty()) {
                    val file = it.createFolder(listOf("root"), ROOT_FOLDER).await()
                    appDirId = file.id
                } else {
                    appDirId = result.files[0].id
                }
            }
        }
    }

    private suspend fun refresh() {
        driveServiceHelper?.let {
            val result = withContext(Dispatchers.IO) {
                it.queryFiles(appDirId).await().files
            }
            _driveFileList.value = result
        }
    }

    private val _requestStatus = MutableLiveData<RequestStatus?>(null)
    val requestStatus: LiveData<RequestStatus?>
        get() = _requestStatus

    private fun makeRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            _requestStatus.value = RequestStatus.InProgress
            try {
                checkAndMakeRootFolder()
                block()
                refresh()
                _requestStatus.value = RequestStatus.Success
            } catch (e: Exception) {
                Log.e(tagLog, "requestException", e)
                _requestStatus.value = RequestStatus.Failure(e)
            } finally {
                _requestStatus.value = null
            }
        }
    }

    private val _blockRequestStatus = MutableLiveData<RequestStatus?>()
    val blockingRequestStatus: LiveData<RequestStatus?>
        get() = _blockRequestStatus

    private fun <T> makeBlockingRequest(block: suspend () -> T): T? {
        var result: T? = null
        viewModelScope.launch {
            _blockRequestStatus.value = RequestStatus.InProgress
            try {
                checkAndMakeRootFolder()
                result = block()
                refresh()
                _blockRequestStatus.value = RequestStatus.Success
            } catch (e: Exception) {
                Log.e(tagLog, "blockingRequestException", e)
                _blockRequestStatus.value = RequestStatus.Failure(e)
            } finally {
                _blockRequestStatus.value = null
            }
        }
        return result
    }

    private fun clearCredentials() {
        driveClient = null
        driveAccount = null
        driveServiceHelper = null
    }
}

sealed class RequestStatus {
    object InProgress : RequestStatus()
    object Success : RequestStatus()
    class Failure(val e: Exception): RequestStatus()
}
