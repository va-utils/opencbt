package com.vva.androidopencbt.export

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.vva.androidopencbt.*
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import com.vva.androidopencbt.settings.ExportFormats
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import java.io.File
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
@HiltViewModel
class ExportViewModel @Inject constructor(application: Application, val dao: RecordDao): AndroidViewModel(application) {
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

    private val _exportState = MutableLiveData<ExportStates?>(null)
    val exportState: LiveData<ExportStates?>
        get() = _exportState

    fun export(export: Export) {
        process(export.fileName, export.isCloud) {
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

            when (export.format) {
                ExportFormats.JSON -> {
                    saveStringToFile(toJson(list), export.fileName)
                }
                ExportFormats.HTML -> {
                    saveStringToFile(toHtml(list, getApplication()), export.fileName)
                }
                ExportFormats.CSV -> {
                    toCsv(list, export.fileName)
                }
            }
        }
    }

    /**
     * @param list - list с данными для сохранения
     * @param fileName имя создаваемого файла
     * @return полный путь к созданному файлу
     */
    private fun toCsv(list: List<DbRecord>, fileName: String): String {
        val strings = getApplication<App>().resources
        val header = listOf(strings.getString(R.string.csv_header_datetime),
                strings.getString(R.string.csv_header_situation),
                strings.getString(R.string.csv_header_thoughts),
                strings.getString(R.string.csv_header_emotions),
                strings.getString(R.string.csv_header_intensity),
                strings.getString(R.string.csv_header_feelings),
                strings.getString(R.string.csv_header_actions),
                strings.getString(R.string.csv_header_distortions),
                strings.getString(R.string.csv_header_rational))

        val filePath = "${getApplication<Application>().cacheDir.absolutePath}/$fileName"
        val csvWriter = CsvWriter()
        csvWriter.open(filePath) {
            writeRow(header)
            list.forEach {
                writeRow(convertRecordToList(it))
            }
        }

        return filePath
    }

    private fun convertRecordToList(record: DbRecord): List<String> {
        return listOf(record.datetime.getShortDateTime(),
                record.situation,
                record.thoughts,
                record.emotions,
                record.intensity.toString(),
                record.feelings,
                record.actions,
                record.getDistortionsString(getApplication()),
                record.rational
        )
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

    private fun process(fileName: String, isCloud: Boolean, block: suspend () -> String) {
        viewModelScope.launch {
            _exportState.value = ExportStates.InProgress
            try {
                val filePath = block()
                _exportState.value = ExportStates.Success(fileName, filePath, isCloud)
            } catch (e: Exception) {
                _exportState.value = ExportStates.Failure(e)
            } finally {
                _exportState.value = null
            }
        }
    }

    /**
     * @param string - строка для записи в файл
     * @param fileName - имя создаваемого файла
     * @return полный путь к созданному файлу
     */
    private suspend fun saveStringToFile(string: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(getApplication<App>().cacheDir, fileName)
            file.writeText(string)
            file.absolutePath
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

            val name: String = when(format) {
                ExportFormats.JSON -> {
                    "$fileName.json"
                }
                ExportFormats.HTML -> {
                    "$fileName.html"
                }
                ExportFormats.CSV -> {
                    "$fileName.csv"
                }
            }

            return Export(name, format, isWholeDiary, beginDate, endDate, exportList, isCloud)
        }
    }

    companion object {
        const val DESTINATION_LOCAL = 0
        const val DESTINATION_CLOUD = 1

        const val FORMAT_JSON = 0
        const val FORMAT_HTML = 1
        const val FORMAT_CSV = 2
        const val FORMAT_PICK = 100
    }
}

sealed class ExportStates {
    object InProgress : ExportStates()
    data class Success(val fileName: String, val filePath: String, val isCloud: Boolean) : ExportStates()
    data class Failure(val e: java.lang.Exception): ExportStates()
}