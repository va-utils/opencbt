package com.vva.androidopencbt.export

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.vva.androidopencbt.beginOfMonth
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.endOfDay
import com.vva.androidopencbt.getShortDateTime
import com.vva.androidopencbt.settings.ExportFormats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime

@Suppress("BlockingMethodInNonBlockingContext")
class ExportViewModelNew(application: Application): AndroidViewModel(application) {
    private val dao = CbdDatabase.getInstance(application).databaseDao

    private val _beginDate = MutableLiveData(DateTime().beginOfMonth())
    val beginDate: LiveData<DateTime>
        get() = _beginDate
    val beginDateTime: DateTime
        get() = _beginDate.value ?: DateTime().beginOfMonth()

    private val _endDate = MutableLiveData(DateTime().endOfDay())
    val endDate: LiveData<DateTime>
        get() = _endDate
    val endDateTime: DateTime
        get() = _endDate.value ?: DateTime().endOfDay()

    fun setBeginDate(dateTime: DateTime) {
        _beginDate.value = dateTime
    }

    fun setEndDate(dateTime: DateTime) {
        _endDate.value = dateTime
    }

    private val _exportState = MutableLiveData<ProcessStates?>(null)
    val exportState: LiveData<ProcessStates?>
        get() = _exportState

    var fileName: String = ""

    fun export(export: Export) {
        fileName = export.fileName
        process {
            val list = export.list
                    ?: withContext(Dispatchers.IO) {
                        if (export.isWholeDiary) {
                            dao.getAllList()
                        } else if (export.begin != null && export.end != null) {
                            dao.getRecordsForPeriod(export.begin, export.end)
                        } else {
                            throw IllegalStateException("Dates is null")
                        }
                    }

            val string = when (export.format) {
                ExportFormats.JSON -> {
                    toJson(list)
                }
                ExportFormats.HTML -> {
                    toHtml(list, getApplication())
                }
            }

            if (export.isCloud) {

            } else {
                saveStringToFile(string, export.fileName)
            }
        }
    }

    private fun toJson(list: List<DbRecord>): String {
        return Json.encodeToString(list)
    }

    private suspend fun toHtml(records: List<DbRecord>, context: Context): String {
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

    private fun process(block: suspend () -> Unit) {
        viewModelScope.launch {
            _exportState.value = ProcessStates.InProgress
            try {
                block()
                _exportState.value = ProcessStates.Success
            } catch (e: Exception) {
                _exportState.value = ProcessStates.Failure(e)
            } finally {
                _exportState.value = null
            }
        }
    }

    private suspend fun saveStringToFile(string: String, fileName: String) {
        withContext(Dispatchers.IO) {
            getApplication<Application>()
                    .openFileOutput(fileName, Context.MODE_PRIVATE).apply {
                            write(string.toByteArray())
                            close()
                    }
        }
    }
}

class Export private constructor(val fileName: String, val format: ExportFormats,
                                 val isWholeDiary: Boolean,
                                 val begin: DateTime? = null, val end: DateTime? = null,
                                 val list: List<DbRecord>? = null,
                                 val isCloud: Boolean) {
    class Builder {
        private var isWholeDiary = true
        private var beginDate: DateTime? = null
        private var endDate: DateTime? = null
        private var format = ExportFormats.JSON
        private var fileName: String = ""
        private var exportList: List<DbRecord>? = null
        private var isCloud: Boolean = false

        fun cloud() : Builder {
            isCloud = true

            return this
        }

        fun setExportList(list: List<DbRecord>): Builder {
            isWholeDiary = false
            exportList = list

            return this
        }

        fun setPeriod(begin: DateTime, end: DateTime): Builder {
            if (!begin.isBefore(end)) {
                throw IllegalStateException("Begin date is not before end date")
            }
            isWholeDiary = false

            beginDate = begin
            endDate = end
            return this
        }

        fun setFormat(format: ExportFormats): Builder {
            this.format = format

            return this
        }

        fun setFileName(name: String): Builder {
            fileName = name

            return this
        }

        fun build(): Export {
            if (fileName.isEmpty())
                throw IllegalStateException("No filename supplied")

            val name = when(format) {
                ExportFormats.JSON -> {
                    "$fileName.json"
                }
                ExportFormats.HTML -> {
                    "$fileName.html"
                }
            }

            return Export(name, format, isWholeDiary, beginDate, endDate, exportList, isCloud)
        }
    }

    companion object {
        const val DESTINATION_LOCAL = 0
        const val DESTINATION_CLOUD = 1
    }
}