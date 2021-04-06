package com.vva.androidopencbt.settings

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.vva.androidopencbt.DOWNLOADS_CHANNEL_ID
import com.vva.androidopencbt.R

const val DOWNLOAD_NOTIFICATION_ID = 0x123

class DriveDownloader(context: Context, preference: Preference) {
    private val appContext = context.applicationContext
    private val manager = SplitInstallManagerFactory.create(appContext)
    private val notificationManager = ContextCompat.getSystemService(appContext, NotificationManager::class.java) as NotificationManager

    private val listener = SplitInstallStateUpdatedListener {
        when (it.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                val builder = NotificationCompat.Builder(appContext, DOWNLOADS_CHANNEL_ID)
                        .setContentText("Downloading modules")
                        .setSmallIcon(R.drawable.ic_baseline_download)
                        .setProgress(it.totalBytesToDownload().toInt(), it.bytesDownloaded().toInt(), false)
                Log.d("NOTIFY", it.bytesDownloaded().toString())
                notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build())

            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
//                manager.startConfirmationDialogForResult(it, context as MainActivity, 0x111)
                context.startIntentSender(it.resolutionIntent()?.intentSender, null, 0, 0, 0)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                val builder = NotificationCompat.Builder(appContext, DOWNLOADS_CHANNEL_ID)
                        .setContentText("Downloading modules")
                        .setSmallIcon(R.drawable.ic_baseline_download_done)
                        .setContentText("Module installed")
                notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build())

            }
            SplitInstallSessionStatus.INSTALLING -> {
                val builder = NotificationCompat.Builder(appContext, DOWNLOADS_CHANNEL_ID)
                        .setContentText("Installing modules")
                        .setSmallIcon(R.drawable.ic_baseline_download_done)
                        .setProgress(0, 0, true)
                notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build())

            }
            SplitInstallSessionStatus.FAILED -> {
                val builder = NotificationCompat.Builder(appContext, DOWNLOADS_CHANNEL_ID)
                        .setContentText("Download failed")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText("FAILED")
                notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build())
                (preference as SwitchPreferenceCompat).isChecked = false
            }
            SplitInstallSessionStatus.CANCELED -> {
                TODO()
            }
            SplitInstallSessionStatus.CANCELING -> {
                TODO()
            }
            SplitInstallSessionStatus.DOWNLOADED -> {
                TODO()
            }
            SplitInstallSessionStatus.PENDING -> {
                TODO()
            }
            SplitInstallSessionStatus.UNKNOWN -> {
                TODO()
            }
        }
    }

    fun download() {
        manager.registerListener(listener)
        val request = SplitInstallRequest.newBuilder()
                .addModule(GDRIVE_MODULE_NAME)
                .build()
        manager.startInstall(request)
    }
}