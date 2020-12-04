package com.vva.androidopencbt.export

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.vva.androidopencbt.beginOfMonth
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.endOfDay
import com.vva.androidopencbt.getShortDateTime
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import java.io.IOException

private const val FILE_NAME_PREFIX = "CBT_diary"

@Suppress("unused")
class ExportViewModel(application: Application) : AndroidViewModel(application) {
    private var _fileName = ""
    val fileName: String
        get() = _fileName

    private val dao = CbdDatabase.getInstance(application).databaseDao
    private val vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)

    private val _isHtmlExportInProgress = MutableLiveData<Boolean>()

    //---для периода
    private val _beginDate = MutableLiveData(DateTime().beginOfMonth())
    val beginDate: LiveData<DateTime>
        get() = _beginDate

    private val _endDate = MutableLiveData(DateTime().endOfDay())
    val endDate: LiveData<DateTime>
        get() = _endDate
    //--------------

    private val _format = MutableLiveData<String>(PreferenceManager.getDefaultSharedPreferences(application.applicationContext).getString("default_export","HTML"))

    val format : LiveData<String>
    get() = _format

    fun setFormat(s : String)
    {
        _format.value = s
    }

    fun setBeginDate(dateTime: DateTime) {
        _beginDate.value = dateTime
    }

    fun setEndDate(dateTime: DateTime) {
        _endDate.value = dateTime
    }

    val isHtmlExportInProgress: LiveData<Boolean>
        get() = _isHtmlExportInProgress

    private val _isHtmlFileReady = MutableLiveData<Boolean>()
    val isHtmlFileReady: LiveData<Boolean>
        get() = _isHtmlFileReady

    fun htmlFileShared() {
        _isHtmlFileReady.value = false
    }

    fun makeExportFile(context: Context) {
        when (format.value/*PreferenceManager.getDefaultSharedPreferences(context).getString("default_export", "HTML")*/) {
            "JSON" -> {
                makeJsonExportFile()
            }
            "HTML" -> {
                makeHtmlExportFile(context)
            }
            else -> {
                throw IllegalArgumentException("No such format")
            }
        }
    }

    fun makeHtmlExportFile(context: Context) {
        _isHtmlExportInProgress.value = true
        _isHtmlFileReady.value = false
        _fileName = "${FILE_NAME_PREFIX}_${beginDate.value!!.toString("dd-MM-yyyy")}_${endDate.value!!.toString("dd-MM-yyyy")}.html"
        uiScope.launch {
            val records = withContext(Dispatchers.IO) {
                dao.getRecordsForPeriod(beginDate.value!!, endDate.value!!)
            }
            val exportString = withContext(Dispatchers.Default) {
                makeHtmlString(records, context)
            }
            withContext(Dispatchers.IO) {
                saveStringToFile(exportString, fileName)
            }
        }
    }

    fun makeJsonExportFile() {
        _isHtmlExportInProgress.value = true
        _isHtmlFileReady.value = false
        _fileName = "${FILE_NAME_PREFIX}_${beginDate.value!!.toString("dd-MM-yyyy")}_${endDate.value!!.toString("dd-MM-yyyy")}.json"
        uiScope.launch {
            val records = withContext(Dispatchers.IO) {
                dao.getRecordsForPeriod(beginDate.value!!, endDate.value!!)
            }
            val exportString = withContext(Dispatchers.Default) {
//                GsonBuilder()
//                Gson().toJson(records)
                Json.encodeToString(records)
            }
            withContext(Dispatchers.IO) {
                saveStringToFile(exportString, fileName)
            }
        }
    }

    private suspend fun makeHtmlString(records: List<DbRecord>, context: Context): String {
        return withContext(Dispatchers.Default) {
            val forHtml = StringBuilder()
            forHtml.append("<!DOCTYPE html>")
            forHtml.append("<html><head><meta content='text/html; charset=utf-8'>")
            forHtml.append("<title>Дневник</title></head>")
            forHtml.append("<body>")
            forHtml.append("<table border='1' width='100%'>")

            //--- Заголовок
            forHtml.append("<tr>")
            forHtml.append("<th>Дата и время</th>")
            forHtml.append("<th>Ситуация</th>")
            forHtml.append("<th>Автоматические мысли</th>")
            forHtml.append("<th>Эмоции</th>")
            forHtml.append("<th>Диск. (в %)</th>")
            forHtml.append("<th>Телесные ощущения</th>")
            forHtml.append("<th>Предпринятые действия</th>")
            forHtml.append("<th>Когнитивные искажения</th>")
            forHtml.append("<th>Рациональный ответ</th>")
            forHtml.append("</tr>")

            //---заполнение таблицы строками
            //---
            //---заполнение таблицы строками
            for (record in records) {
                forHtml.append("<tr>")
                forHtml.append("<td>").append(record.datetime.getShortDateTime()).append("</td>")
                forHtml.append("<td>").append(record.situation).append("</td>")
                forHtml.append("<td>").append(record.thoughts).append("</td>")
                forHtml.append("<td>").append(record.emotions).append("</td>")
                forHtml.append("<td>").append(record.intensity).append("</td>")
                forHtml.append("<td>").append(record.feelings).append("</td>")
                forHtml.append("<td>").append(record.actions).append("</td>")
                forHtml.append("<td>").append(record.getDistortionsString(context)).append("</td>")
                forHtml.append("<td>").append(record.rational).append("</td>")
                forHtml.append("</tr>")
            }
            //--------------
            //--------------
            forHtml.append("</table>")
            forHtml.append("</body>")
            forHtml.append("</html>")
            forHtml.toString()
        }
    }

    private suspend fun saveStringToFile(string: String, fileName: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                getApplication<Application>()
                        .openFileOutput(fileName, Context.MODE_PRIVATE).apply {
                            try {
                                write(string.toByteArray())
                                close()

                                withContext(Dispatchers.Main) {
                                    _isHtmlFileReady.value = true
                                    _isHtmlExportInProgress.value = false
                                }
                            } catch (e: IOException) {
                                withContext(Dispatchers.Main) {
                                    _isHtmlFileReady.value = false
                                    _isHtmlExportInProgress.value = false
                                }
                            }
                        }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }
}