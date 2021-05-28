package com.vva.androidopencbt.playfeatures

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.vva.androidopencbt.settings.GDRIVE_MODULE_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeatureDownloadViewModel @Inject constructor(private val splitInstallManager: SplitInstallManager): ViewModel() {
    private val _installState = MutableLiveData<ProcessState?>(null)
    val installState: LiveData<ProcessState?>
        get() = _installState

    private val listener = SplitInstallStateUpdatedListener {
        when (it.status()) {
            SplitInstallSessionStatus.CANCELED -> {
                _installState.value = ProcessState.Canceled
                _installState.value = null
            }
            SplitInstallSessionStatus.CANCELING -> {
                _installState.value = ProcessState.InProgress(InProgressState.Cancelling)
            }
            SplitInstallSessionStatus.DOWNLOADED -> {
                _installState.value = ProcessState.InProgress(InProgressState.Downloaded)
            }
            SplitInstallSessionStatus.DOWNLOADING -> {
                val maxInPercent = 100
                val downloadedInPercent: Int = (it.bytesDownloaded() / (it.totalBytesToDownload() / 100)).toInt()
                _installState.value = ProcessState.InProgress(InProgressState.Downloading(maxInPercent, downloadedInPercent))
            }
            SplitInstallSessionStatus.FAILED -> {
                _installState.value = ProcessState.Failure(IllegalStateException("${it.errorCode()}"))
                _installState.value = null
            }
            SplitInstallSessionStatus.INSTALLED -> {
                _installState.value = ProcessState.Success
                _installState.value = null
            }
            SplitInstallSessionStatus.INSTALLING -> {
                _installState.value = ProcessState.InProgress(InProgressState.Installing)
            }
            SplitInstallSessionStatus.PENDING -> {
                _installState.value = ProcessState.InProgress(InProgressState.Pending)
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                _installState.value = ProcessState.InProgress(InProgressState.RequiresUserConfirmation)
            }
            SplitInstallSessionStatus.UNKNOWN -> {
                _installState.value = ProcessState.InProgress(InProgressState.Unknown)
            }
        }
    }

    var id: Int = 0
    fun driveInstallCancel() {
        splitInstallManager.cancelInstall(id)
    }

    fun driveFeatureDownload() {
        splitInstallManager.registerListener(listener)
        splitInstallManager.startInstall (
            SplitInstallRequest.newBuilder().addModule(GDRIVE_MODULE_NAME).build()
        ).addOnSuccessListener {
            id = it
        }
    }

    sealed class ProcessState {
        data class InProgress(val state: InProgressState): ProcessState()
        object Success: ProcessState()
        object Canceled: ProcessState()
        data class Failure(val e: Exception): ProcessState()
    }

    sealed class InProgressState {
        data class Downloading(val max: Int, val progress: Int): InProgressState()
        object RequiresUserConfirmation : InProgressState()
        object Installing : InProgressState()
        object Cancelling : InProgressState()
        object Downloaded : InProgressState()
        object Pending : InProgressState()
        object Unknown : InProgressState()
    }
}
