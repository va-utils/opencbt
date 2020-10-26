package com.vva.androidopencbt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord

class RecordsViewModel(application: Application): AndroidViewModel(application) {
    private val db = CbdDatabase.getInstance(application)

    fun getAllRecords() = db.databaseDao.getAll()

    fun getRecordById(id: Long) = db.databaseDao.getById(id)

    fun deleteRecord(record: DbRecord) {
        db.databaseDao.deleteRecord(record)
    }

    fun updateRecord(record: DbRecord) {
        db.databaseDao.updateRecord(record)
    }
}