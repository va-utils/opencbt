package com.vva.androidopencbt.gdrivefeature

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Executors

class DriveServiceHelper private constructor(private val mDriveService: Drive) {
    private val mExecutor = Executors.newSingleThreadExecutor()

    fun createFile(parents: String?, mimeType: String, fileName: String): Task<String> {
        return Tasks.call(mExecutor) {
            val metadata = File()
                    .setParents(Collections.singletonList(parents))
                    .setMimeType(mimeType)
                    .setName(fileName)

            val googleFile = mDriveService.files().create(metadata).execute()
                    ?: throw IOException("Null result when requesting file creation")

            googleFile.id
        }
    }

    fun createFolder(parents: List<String>, name: String): Task<File> {
        return Tasks.call(mExecutor) {
            val metadata = File()
                    .setParents(parents)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(name)

            mDriveService.Files().create(metadata).execute()
        }
    }

    fun checkFolderExist(name: String): Task<FileList> {
        return Tasks.call(mExecutor){
            mDriveService.files()
                    .list()
                    .setQ("name='$name' and mimeType = 'application/vnd.google-apps.folder'").execute()
        }
    }

    fun readFile(fileId: String): Task<Pair<String, String>> {
        return Tasks.call(mExecutor) {
            val metadata = mDriveService
                    .files()
                    .get(fileId)
                    .execute()
            val name = metadata.name

            mDriveService.files().get(fileId).executeAsInputStream().use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { bufferedReader ->
                    Pair<String, String>(name, bufferedReader.readText())
                }
            }
        }
    }

    fun saveFile(fileId: String, name: String, content: String, mimeType: String): Task<File> {
        return Tasks.call(mExecutor) {
            val metadata = File()
                    .setName(name)

            val contentStream = ByteArrayContent.fromString(mimeType, content)

            mDriveService.files().update(fileId, metadata, contentStream).execute()
        }
    }

    fun uploadFile(parents: List<String>, fileName: String, filePath: String): Task<File> {
        val metadata = File()
                .setParents(parents)
                .setName(fileName)

        val localFile = java.io.File(filePath)
        val content = FileContent("application/octet-stream", localFile)
        return Tasks.call(mExecutor) {
            mDriveService.files()
                    .create(metadata, content)
                    .setFields("id")
                    .execute()
        }
    }

    fun queryFiles(folderId: String): Task<FileList> {
        return Tasks.call(mExecutor) {
            mDriveService.files().list()
                    .setQ("parents in '$folderId'")
                    .setFields("files(id, name, createdTime, size)")
                    .execute()
        }
    }

    suspend fun queryLastFiles(): FileList {
        return suspendCancellableCoroutine { continuation ->
            val task = Tasks.call(mExecutor) {
                mDriveService.files().list().setSpaces("drive").execute()
            }
            task.addOnCompleteListener {
                continuation.resume(task.result!!) {

                }
            }
        }
    }

    companion object {
        private val INSTANCE: DriveServiceHelper? = null

        fun getInstance(credential: GoogleAccountCredential): DriveServiceHelper {
            return INSTANCE ?: DriveServiceHelper(getDrive(credential))
        }

        private fun getDrive(credential: GoogleAccountCredential): Drive {
            return Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
            ).setApplicationName("OpenCBT")
                    .build()
        }
    }
}